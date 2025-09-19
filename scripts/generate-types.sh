#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-1.0.7}"
REPO="https://github.com/hyodotdev/openiap-gql"
ASSET="openiap-kotlin.zip"
DOWNLOAD_URL="${REPO}/releases/download/${VERSION}/${ASSET}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
TARGET_DIR="${REPO_ROOT}/openiap/src/main/java/dev/hyo/openiap"
TARGET_FILE="${TARGET_DIR}/Types.kt"

TMP_DIR="$(mktemp -d)"
cleanup() {
  rm -rf "${TMP_DIR}"
}
trap cleanup EXIT

printf 'Downloading %s\n' "${DOWNLOAD_URL}"
curl -fL "${DOWNLOAD_URL}" -o "${TMP_DIR}/${ASSET}"

printf 'Extracting Types.kt\n'
unzip -q "${TMP_DIR}/${ASSET}" -d "${TMP_DIR}"

mkdir -p "${TARGET_DIR}"
rm -f "${TARGET_FILE}"
cp "${TMP_DIR}/Types.kt" "${TARGET_FILE}"

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

# Ensure there is exactly one blank line between annotations and the package declaration
package_idx = first_index(lambda line: line.startswith('package '))
if package_idx is not None:
    before_idx = package_idx - 1
    if before_idx >= 0 and lines[before_idx].strip():
        lines.insert(package_idx, '')
        package_idx += 1
    after_idx = package_idx + 1
    if after_idx >= len(lines) or lines[after_idx].strip():
        lines.insert(package_idx + 1, '')

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
target.write_text(text)
PY

printf 'Types.kt updated at %s\n' "${TARGET_FILE}"
