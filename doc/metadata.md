# Package Metadata (PackageMetadata)

Similar to `mods.toml`, PackageMetadata is a structured JSON format that describes the metadata of a package used for
define providing information about the package, for example name, authors, and dependencies on other packages.

This format is primarily used in package management systems for dependency resolution, version control, and load order
management.

It exists within the packages as a file named `kube_packags.json`.

## Basic Example

```json
{
  "id": "example_pkg",
  "name": "ExamplePackage",
  "description": "An example package demonstrating metadata format",
  "version": "1.2.3",
  "authors": [
    "Zank"
  ],
  "dependencies": [
    {
      "type": "REQUIRED",
      "id": "common_utils",
      "versionRange": "[1.2,)",
      "ordering": "AFTER"
    },
    {
      "type": "INCOMPATIBLE",
      "id": "some_random_bad_mod",
      "source": "MOD",
      "versionRange": "[3.0,)",
      "reason": "The mod changed its injection point after 3.0, invalidating modifications from this package"
    }
  ]
}
```

## Basic Fields

| Field Name   | Type            | Required | Description                                                                  |
|--------------|-----------------|----------|------------------------------------------------------------------------------|
| id           | String          | √        | Unique identifier for the package, follow naming rules similar to mod IDs.   |
| name         | String          |          | Display name of the package                                                  |
| description  | String          |          | Detailed description of the package                                          |
| version      | String          | √        | Version number of the package, following semantic versioning (e.g., "1.0.0") |
| license      | String          |          | License of the package                                                       |
| authors      | String List     |          | List of authors of the package                                               |
| dependencies | Dependency List |          | List of dependencies for the package                                         |

## Dependency Fields

Each dependency is an object containing the following fields:

| Field Name   | Type   | Required | Default Value | Description                                                   |
|--------------|--------|----------|---------------|---------------------------------------------------------------|
| type         | String | √        | -             | Dependency type, see Dependency Types                         |
| source       | String |          | "PACK"        | Dependency source, "PACK" (package) or "MOD" (mod)            |
| id           | String | √        | -             | Unique identifier of the dependency                           |
| versionRange | String |          | -             | Version range specification, using Maven version range syntax |
| reason       | String |          | -             | Description of the reason for the dependency                  |
| ordering     | String |          | "NONE"        | Load order requirement, see Load Order                        |

## Dependency Types

| Value        | Severity | Description                                                                                    |   |
|--------------|----------|------------------------------------------------------------------------------------------------|---|
| REQUIRED     | Error    | Required dependency - loading will fail if missing                                             |   |
| OPTIONAL     | Info     | Optional dependency - loading will not fail if missing, but participates in dependency sorting |   |
| RECOMMENDED  | Warning  | Recommended dependency - serves a metadata hint                                                |   |
| DISCOURAGED  | Warning  | Discouraged dependency - generates a warning when both are present                             |   |
| INCOMPATIBLE | Error    | Incompatible dependency - loading will fail when both are present                              |   |

## Load Order

| Value  | Description                                                 |
|--------|-------------------------------------------------------------|
| NONE   | No special load order requirement                           |
| BEFORE | Current package should load before the specified dependency |
| AFTER  | Current package should load after the specified dependency  |

## Version Range Syntax

Version ranges use the Maven version range specification:

- `"1.0"` - Exact version 1.0
- `"[1.0,2.0)"` - Versions from 1.0 (inclusive) to 2.0 (exclusive)
- `"(1.0,2.0]"` - Versions from 1.0 (exclusive) to 2.0 (inclusive)
- `"[1.0,)"` - Version 1.0 and above
- `"(,1.0]"` - Version 1.0 and below

## Example

```json
{
  "id": "fullpack",
  "name": "Full Feature Pack",
  "description": "A pack demonstrating all metadata features",
  "version": "2.3.0-beta",
  "authors": [
    "Developer Team",
    "Contributor"
  ],
  "dependencies": [
    {
      "type": "REQUIRED",
      "id": "common_utils",
      "versionRange": "[1.2.3,4.5.6)",
      "ordering": "AFTER"
    },
    {
      "type": "OPTIONAL",
      "id": "debug_trigger",
      "versionRange": "[1.0,)",
      "reason": "Install it to enable debug mode"
    },
    {
      "type": "INCOMPATIBLE",
      "id": "examplemod",
      "source": "MOD",
      "reason": "really? ExampleMod in production environment?"
    }
  ]
}
```