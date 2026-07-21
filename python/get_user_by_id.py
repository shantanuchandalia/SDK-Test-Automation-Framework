"""SDK demo method: getUserById (Python implementation).

Reads user records from the shared flat file (data/users.json) and
returns the username for a given user ID.

Usage:
    python get_user_by_id.py <id>     # look up a specific ID
    python get_user_by_id.py          # runs a built-in demo lookup (ID 101)
"""

import json
import sys
from pathlib import Path

# Shared flat file lives at <repo-root>/data/users.json
DATA_FILE = Path(__file__).resolve().parent.parent / "data" / "users.json"

DEMO_ID = 101


def get_user_by_id(user_id: int) -> str | None:
    """Return the username for the given user ID, or None if not found."""
    with open(DATA_FILE, encoding="utf-8") as f:
        data = json.load(f)
    for user in data.get("users", []):
        if user.get("id") == user_id:
            return user.get("username")
    return None


def main() -> int:
    if len(sys.argv) > 1:
        try:
            user_id = int(sys.argv[1])
        except ValueError:
            print(f"ERROR: '{sys.argv[1]}' is not a valid integer ID")
            return 2
    else:
        print(f"No ID supplied - running demo lookup for ID {DEMO_ID}")
        user_id = DEMO_ID

    username = get_user_by_id(user_id)
    if username is None:
        print(f"User with ID {user_id} not found")
        return 1

    print(f"getUserById({user_id}) -> {username}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
