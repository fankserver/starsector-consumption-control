#!/usr/bin/env python3
import re
import sys

SEMVER = re.compile(
    r"^(0|[1-9][0-9]*)\."
    r"(0|[1-9][0-9]*)\."
    r"(0|[1-9][0-9]*)"
    r"(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?"
    r"(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$"
)


def parse_tag(tag: str) -> str:
    if not tag.startswith("v"):
        raise ValueError("release tag must start with 'v' (example: v1.2.3)")
    version = tag[1:]
    match = SEMVER.fullmatch(version)
    if not match:
        raise ValueError(f"invalid semantic version tag: {tag}")
    prerelease = match.group(4)
    if prerelease:
        for identifier in prerelease.split("."):
            if identifier.isdigit() and len(identifier) > 1 and identifier.startswith("0"):
                raise ValueError(f"numeric prerelease identifiers may not contain leading zeroes: {tag}")
    return version


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"usage: {sys.argv[0]} vMAJOR.MINOR.PATCH", file=sys.stderr)
        raise SystemExit(2)
    try:
        print(parse_tag(sys.argv[1]))
    except ValueError as error:
        print(error, file=sys.stderr)
        raise SystemExit(1)
