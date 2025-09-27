#!/usr/bin/env bash
set -euo pipefail

VERSION=""
SKIP_DOWNLOAD=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-download)
      SKIP_DOWNLOAD=true
      shift
      ;;
    --version)
      if [[ $# -lt 2 ]]; then
        echo "--version requires an argument" >&2
        exit 1
      fi
      shift 2
      ;;
    --help)
      cat <<'EOF'
Usage: ./scripts/generate-types.sh [--version <tag>] [--skip-download]

Options:
  --version         Release tag to download from openiap-gql (default: VERSION file)
  --skip-download   Reuse the existing Types.kt and only run post-processing
EOF
      exit 0
      ;;
    --*)
      echo "Unknown option: $1" >&2
      exit 1
      ;;
    *)
      if [[ -n "$VERSION" ]]; then
        echo "Unexpected argument: $1" >&2
        exit 1
      fi
      VERSION="$1"
      shift
      ;;
  esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
TARGET_DIR="${REPO_ROOT}/openiap/src/main/java/dev/hyo/openiap"
TARGET_FILE="${TARGET_DIR}/Types.kt"
VERSION_FILE="${REPO_ROOT}/VERSION"
VERSIONS_JSON="${REPO_ROOT}/openiap-versions.json"

# Try to get version from openiap-versions.json first
if [[ -z "$VERSION" ]] && [[ -f "$VERSIONS_JSON" ]]; then
  VERSION="$(python3 -c "import json; print(json.load(open('$VERSIONS_JSON'))['gql'])" 2>/dev/null || true)"
fi

# Fall back to VERSION file if not found
if [[ -z "$VERSION" ]] && [[ -f "$VERSION_FILE" ]]; then
  VERSION="$(head -n1 "$VERSION_FILE" | tr -d ' \r')"
fi

if [[ -z "$VERSION" ]]; then
  echo "Unable to determine version. Provide --version or make sure VERSION file exists." >&2
  exit 1
fi

REPO="https://github.com/hyodotdev/openiap-gql"
ASSET="openiap-kotlin.zip"
DOWNLOAD_URL="${REPO}/releases/download/${VERSION}/${ASSET}"

mkdir -p "${TARGET_DIR}"

TMP_DIR=""
cleanup() {
  if [[ -n "$TMP_DIR" && -d "$TMP_DIR" ]]; then
    rm -rf "$TMP_DIR"
  fi
}

if [[ "$SKIP_DOWNLOAD" == false ]]; then
  TMP_DIR="$(mktemp -d)"
  trap cleanup EXIT

  printf 'Downloading %s\n' "${DOWNLOAD_URL}"
  curl -fL "${DOWNLOAD_URL}" -o "${TMP_DIR}/${ASSET}"

  printf 'Extracting Types.kt\n'
  unzip -q "${TMP_DIR}/${ASSET}" -d "${TMP_DIR}"

  rm -f "${TARGET_FILE}"
  cp "${TMP_DIR}/Types.kt" "${TARGET_FILE}"
else
  if [[ ! -f "${TARGET_FILE}" ]]; then
    echo "Types.kt not found at ${TARGET_FILE}; cannot skip download" >&2
    exit 1
  fi
  printf 'Skipping download; reusing existing %s\n' "${TARGET_FILE}"
fi

# Patch known Kotlin formatting issues in the upstream artifact so the file compiles
TARGET_FILE="${TARGET_FILE}" python3 <<'PY'
from pathlib import Path
import os
import re

target = Path(os.environ["TARGET_FILE"])
text = target.read_text()

lines = text.splitlines()

def first_index(predicate):
    for idx, line in enumerate(lines):
        if predicate(line):
            return idx
    return None


package_idx = first_index(lambda line: line.startswith('package '))

annotation_indices = [idx for idx, line in enumerate(lines) if line.startswith('@file:')]

if package_idx is None:
    insert_idx = annotation_indices[0] + 1 if annotation_indices else 0
    lines.insert(insert_idx, 'package dev.hyo.openiap')
    package_idx = insert_idx
else:
    lines[package_idx] = 'package dev.hyo.openiap'

if annotation_indices and annotation_indices[0] > package_idx:
    annotation_block = [lines[idx] for idx in annotation_indices]
    for idx in reversed(annotation_indices):
        lines.pop(idx)
    package_idx = first_index(lambda line: line.startswith('package '))
    for offset, line in enumerate(annotation_block):
        lines.insert(package_idx + offset, line)
    package_idx += len(annotation_block)

text = '\n'.join(lines)

# Kotlin enums that declare a companion object require a trailing semicolon
enum_pattern = re.compile(r"(\n\s*\w+\([^)]*\))\n\n(\s+companion object)")
text = enum_pattern.sub(lambda m: f"{m.group(1)};\n\n{m.group(2)}", text)

# Ensure data classes implementing shared interfaces mark interface properties with override
class_pattern = re.compile(
    r"public data class [^(]+\((?P<body>.*?)\)\s*:\s*(?P<interfaces>[^\{]+)\{",
    re.S,
)

product_props = {
    "currency",
    "debugDescription",
    "description",
    "displayName",
    "displayPrice",
    "id",
    "platform",
    "price",
    "title",
    "type",
}

purchase_props = {
    "id",
    "ids",
    "isAutoRenewing",
    "platform",
    "productId",
    "purchaseState",
    "purchaseToken",
    "quantity",
    "transactionDate",
}

def needs_product_common(interfaces):
    return any(name in interfaces for name in ("ProductCommon", "Product", "ProductSubscription"))


def needs_purchase_common(interfaces):
    return any(name in interfaces for name in ("PurchaseCommon", "Purchase"))


def patch_class(match):
    body = match.group("body")
    raw_interfaces = match.group("interfaces")
    interfaces = {token.strip() for token in raw_interfaces.replace("\n", " ").split(",")}

    override_targets = set()
    if needs_product_common(interfaces):
        override_targets.update(product_props)
    if needs_purchase_common(interfaces):
        override_targets.update(purchase_props)

    if not override_targets:
        return match.group(0)

    prop_pattern = re.compile(r"(^\s*)(val|var)\s+(\w+)(.*)$", re.M)

    def replace_prop(prop_match):
        indent, keyword, name, rest = prop_match.groups()
        if name not in override_targets:
            return prop_match.group(0)
        # Avoid double prefixing if the generator ever adds override in the future
        if keyword.startswith("override"):
            return prop_match.group(0)
        return f"{indent}override {keyword} {name}{rest}"

    patched_body = prop_pattern.sub(replace_prop, body)
    return match.group(0).replace(body, patched_body)


text = class_pattern.sub(patch_class, text)

lines = text.splitlines()

pattern1 = re.compile(r'(.)([A-Z][a-z0-9]+)')
pattern2 = re.compile(r'([a-z0-9])([A-Z])')


def camel_to_kebab(name: str) -> str:
    s1 = pattern1.sub(r'\1-\2', name)
    s2 = pattern2.sub(r'\1-\2', s1)
    return s2.replace('_', '-').lower()


i = 0
while i < len(lines):
    line = lines[i]
    header_match = re.match(r'^public enum class (\w+)\(val rawValue: String\) \{$', line)
    if not header_match:
        i += 1
        continue
    enum_name = header_match.group(1)

    constant_indices = []
    j = i + 1
    while j < len(lines):
        constant_indices.append(j)
        if lines[j].strip().endswith(';'):
            break
        j += 1
    if not constant_indices:
        i = j
        continue

    constants = []
    for idx in constant_indices:
        const_line = lines[idx]
        match = re.match(r'^(\s*)(\w+)\("([^"]+)"\)(,|;)$', const_line)
        if not match:
            continue
        indent, name, old_raw, trailing = match.groups()
        new_raw = camel_to_kebab(name)
        constants.append(
            {
                "index": idx,
                "indent": indent,
                "name": name,
                "old_raw": old_raw,
                "new_raw": new_raw,
                "trailing": trailing,
            }
        )
        if old_raw != new_raw:
            lines[idx] = f'{indent}{name}("{new_raw}"){trailing}'

    k = j + 1
    while k < len(lines) and 'when (value)' not in lines[k]:
        k += 1
    if k >= len(lines):
        i = j
        continue

    case_start = k + 1
    else_idx = case_start
    while else_idx < len(lines) and 'else ->' not in lines[else_idx]:
        else_idx += 1
    if else_idx >= len(lines):
        i = j
        continue

    while case_start < else_idx and not lines[case_start].strip():
        case_start += 1
    if case_start >= else_idx:
        i = else_idx
        continue

    indent_match = re.match(r'^(\s*)', lines[case_start])
    case_indent = indent_match.group(1) if indent_match else ' ' * 12

    new_case_lines = []
    for const in constants:
        seen = set()
        candidates = [const["new_raw"], const["old_raw"], const["name"]]
        if const["name"].endswith("Ios"):
            ios_upper = const["name"][:-3] + "IOS"
            if ios_upper:
                candidates.append(ios_upper)
        for candidate in candidates:
            if candidate and candidate not in seen:
                new_case_lines.append(
                    f'{case_indent}"{candidate}" -> {enum_name}.{const["name"]}'
                )
                seen.add(candidate)

    lines[case_start:else_idx] = new_case_lines
    i = else_idx

text = '\n'.join(lines)
if not text.endswith('\n'):
    text += '\n'

target.write_text(text)
PY

printf 'Types.kt updated at %s\n' "${TARGET_FILE}"
