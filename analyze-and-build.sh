#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

timestamp="$(date +%Y%m%d_%H%M%S)"
backup_dir="${ROOT}/../demo-backup-${timestamp}"
report="${ROOT}/analysis-report-${timestamp}.txt"

log() { printf '%s\n' "$*" | tee -a "$report"; }

log "=== Project analysis: $(date -Is) ==="
log "Project: $ROOT"
log "Backup:  $backup_dir"

# پیش از هر اقدام، وضعیت و شاخه را ثبت می‌کنیم.
branch="$(git branch --show-current 2>/dev/null || true)"
log "Current branch: ${branch:-unknown}"
log ""
log "=== Initial Git status ==="
git status --short --branch 2>&1 | tee -a "$report" || true

# تهیهٔ نسخهٔ پشتیبان خارج از مخزن؛ هیچ فایلی بازنویسی نمی‌شود.
mkdir -p "$backup_dir"
if command -v rsync >/dev/null 2>&1; then
  rsync -a \
    --exclude='.git' \
    --exclude='target' \
    --exclude='backup-*' \
    --exclude='analysis-report-*.txt' \
    "$ROOT/" "$backup_dir/"
else
  tar -C "$(dirname "$ROOT")" \
    --exclude="$(basename "$ROOT")/.git" \
    --exclude="$(basename "$ROOT")/target" \
    -czf "${backup_dir}.tar.gz" "$(basename "$ROOT")"
  backup_dir="${backup_dir}.tar.gz"
fi
log "Backup created: $backup_dir"

log ""
log "=== Root files ==="
find "$ROOT" -maxdepth 2 -type f \
  ! -path '*/.git/*' ! -path '*/target/*' \
  -printf '%P\n' | sort | tee -a "$report"

log ""
log "=== Java / Spring / Maven facts ==="
java -version 2>&1 | tee -a "$report" || true
if command -v mvn >/dev/null 2>&1; then mvn -version 2>&1 | tee -a "$report"; fi
[[ -f ./mvnw ]] && log "Maven wrapper: present" || log "Maven wrapper: absent"
[[ -f ./pom.xml ]] && log "pom.xml: present" || log "ERROR: pom.xml missing"
grep -nE 'spring-boot|<java.version>|<maven.compiler|<artifactId>' pom.xml 2>/dev/null \
  | head -80 | tee -a "$report" || true

log ""
log "=== Source files and duplicate public class names ==="
find src -type f -name '*.java' -print 2>/dev/null | sort | tee -a "$report" || true
if command -v python3 >/dev/null 2>&1; then
python3 - "$ROOT" <<'PY' | tee -a "$report"
import collections, pathlib, re, sys
root = pathlib.Path(sys.argv[1])
classes = collections.defaultdict(list)
for p in (root / "src").rglob("*.java"):
    text = p.read_text(encoding="utf-8", errors="replace")
    for name in re.findall(r"\bpublic\s+(?:abstract\s+|final\s+)?(?:class|interface|enum|record)\s+(\w+)", text):
        classes[name].append(str(p.relative_to(root)))
print("Duplicate public type names:")
found = False
for name, paths in sorted(classes.items()):
    if len(paths) > 1:
        found = True
        print(f"  {name}: " + " | ".join(paths))
if not found:
    print("  none found")
print("Import lines:")
for p in sorted((root / "src").rglob("*.java")):
    for n, line in enumerate(p.read_text(encoding="utf-8", errors="replace").splitlines(), 1):
        if re.match(r"\s*import\s+", line):
            print(f"  {p.relative_to(root)}:{n}: {line.strip()}")
PY
fi

log ""
log "=== Encoding / BOM scan (Java, properties, XML, YAML) ==="
if command -v python3 >/dev/null 2>&1; then
python3 - "$ROOT" <<'PY' | tee -a "$report"
import pathlib, sys
root = pathlib.Path(sys.argv[1])
exts = {".java", ".properties", ".xml", ".yml", ".yaml"}
for p in sorted(root.rglob("*")):
    if not p.is_file() or p.suffix.lower() not in exts:
        continue
    if any(part in {".git", "target", "backup"} for part in p.parts):
        continue
    b = p.read_bytes()
    issues = []
    if b.startswith(b"\xef\xbb\xbf"): issues.append("UTF-8 BOM")
    if b"\x00" in b: issues.append("NUL byte")
    try: b.decode("utf-8")
    except UnicodeDecodeError as e: issues.append(f"invalid UTF-8 at byte {e.start}")
    if issues: print(f"{p.relative_to(root)}: {', '.join(issues)}")
print("Scan complete (no output above means no flagged encoding issues).")
PY
fi

log ""
log "=== Basic secret-pattern scan (review findings manually) ==="
if command -v grep >/dev/null 2>&1; then
  grep -RInE \
    --exclude-dir=.git --exclude-dir=target --exclude='*.class' \
    '(password|secret|token|api[_-]?key)[[:space:]]*[:=][[:space:]]*["'\''][^"'\'']{6,}' \
    "$ROOT/src" "$ROOT"/*.yml "$ROOT"/*.yaml "$ROOT"/*.properties 2>/dev/null \
    | tee -a "$report" || true
fi

log ""
log "=== Build and tests ==="
if [[ -x ./mvnw ]]; then
  ./mvnw clean verify 2>&1 | tee -a "$report" || build_status=${PIPESTATUS[0]}
elif command -v mvn >/dev/null 2>&1; then
  mvn clean verify 2>&1 | tee -a "$report" || build_status=${PIPESTATUS[0]}
else
  log "SKIPPED: Maven wrapper and mvn are unavailable."
  build_status=127
fi
build_status="${build_status:-0}"
log "Build exit code: $build_status"

log ""
log "=== Final Git status ==="
git status --short --branch 2>&1 | tee -a "$report" || true
log "Report: $report"
log "Backup: $backup_dir"

if [[ "$build_status" -ne 0 ]]; then
  log "Build/tests failed. No commit or push was performed."
  exit "$build_status"
fi

log "Analysis/build completed. No files were staged, committed, or pushed."
