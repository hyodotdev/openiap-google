#!/usr/bin/env bash
set -euo pipefail

# Thin wrapper to allow using .sh suffix, e.g.:
#   ./scripts/bump-version.sh patch|minor|major
#   ./scripts/bump-version.sh 1.2.3

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$SCRIPT_DIR/bump-version" "$@"

