#!/bin/bash
set -e

echo "Running Flyway Immutability Check..."

# Ensure origin/main is fetched for comparison
# The checkout action with fetch-depth: 0 should already have the history, but just in case:
git fetch origin main || true

# Check V__*.sql migrations in db/migration
# We look for Modified (M), Deleted (D), or Renamed (R) files
V_VIOLATIONS=$(git diff origin/main...HEAD --name-status | grep -E '^[MDR].*src/main/resources/db/migration/V__.*\.sql$' || true)

if [ -n "$V_VIOLATIONS" ]; then
    echo "ERROR: Flyway V__ (Versioned) migrations cannot be modified, deleted, or renamed!"
    echo "Violating files:"
    echo "$V_VIOLATIONS"
    exit 1
fi

# Check R__*.sql migrations in db/seed
# We look for Deleted (D) or Renamed (R) files. (M is allowed for repeatable migrations)
R_VIOLATIONS=$(git diff origin/main...HEAD --name-status | grep -E '^[DR].*src/main/resources/db/seed/R__.*\.sql$' || true)

if [ -n "$R_VIOLATIONS" ]; then
    echo "ERROR: Flyway R__ (Repeatable) seed migrations cannot be deleted or renamed!"
    echo "Violating files:"
    echo "$R_VIOLATIONS"
    exit 1
fi

echo "Flyway immutability check passed."
