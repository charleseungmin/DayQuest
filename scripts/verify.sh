#!/usr/bin/env bash
set -euo pipefail

# 기계 검증 게이트: build/lint/test 순서로 실행하고 실패 시 즉시 non-zero 종료한다.
run_step() {
  local step="$1"
  local cmd="$2"

  echo "[verify] START ${step}: ${cmd}"
  if eval "$cmd"; then
    echo "[verify] PASS ${step}"
  else
    local code=$?
    echo "[verify] FAIL ${step} (exit=${code})" >&2
    return "$code"
  fi
}

run_step "build" "./gradlew build"
run_step "lint" "./gradlew lint"
run_step "test" "./gradlew test"

echo "[verify] ALL PASS"
