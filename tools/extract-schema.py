"""This script extracts a SQLite3 schema from a JSON Room-schema file.

I.e. the JSON schema files generated at compile-time, found in app/schemas/
"""

USAGE = "python -m extract-schema <jsonfile> [<dest>] [options]"

#  Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.

import argparse
from pathlib import Path
import sys
import json

# *********************************************************
# functions
# *********************************************************

def exit_with_error(msg: str):
    print("ERROR:", msg, file=sys.stderr)
    sys.exit(1)

def extract_schema_from_entity(entity_json:dict)->str:
    table_name = entity_json["tableName"]
    sql = entity_json["createSql"]
    return sql.replace(r"${TABLE_NAME}", table_name) + ";"

# *********************************************************
# main
# *********************************************************
if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description=__doc__,
        usage=USAGE,
        formatter_class=argparse.RawDescriptionHelpFormatter
    )

    parser.add_argument("jsonfile", help="The JSON Room-schema file to extract from.")
    parser.add_argument("dest", nargs="?", help="The destination for the schema file. If no option is given, the current directory is used. If this is a directory or no option is given, the filename 'db.schema' is used. This must be in an existing directory.")

    args = parser.parse_args()

    jsonfile = Path(args.jsonfile)
    dest = args.dest

    if dest is None: dest = Path(".") / "db.schema"
    else: dest = Path(dest)
    if dest.is_dir(): dest = dest / "db.schema"

    # TODO [21-08-10 14:49] -- check args for validity.

    with jsonfile.open() as jf:
        json_schema = json.load(jf)

    entities_schema = [extract_schema_from_entity(entity_json) for entity_json in json_schema["database"]["entities"]]

    with dest.open("w") as dest_file:
        dest_file.writelines("\n".join(entities_schema))

    print("Done!")
    print(f"Created schema file: {str(dest)}")





