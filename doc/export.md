# Exporting packages

[Example: export.js](example/export.js)

Use `KubePackages.packageExporter()` to get a new exporter instance, and call `.run()` or `.runAsync()` to trigger package exporting.

## Common Properties

| Name            | Type                        | Mandatory | Description                                                               |
|-----------------|-----------------------------|-----------|---------------------------------------------------------------------------|
| exportName      | String                      |           | Name of exported file/directory                                           |
| scriptTypes     | ScriptType[]                |           | Included script types                                                     |
| resourceTypes   | PackType[]                  |           | Included resource types (client_resources -> assets, server_data -> data) |
| exportAs        | ExportType                  | Yes       | Type of exported package (`.zip`, `.jar`, or directory)                   |
| metadata        | PackageMetadata             | Yes       | Metadata of exported package                                              |
| modInfoModifier | (SimulatedModsToml) => void |           | Modify generated `mods.toml`, useful only when `exportAs` is `mod`        |
| silent          | boolean                     |           | Set to `false` to minimize console logs                                   |

## Filtering files

- `filterScriptFile(ScriptType[], (helper) => filter)`: Set the filter for script files
- `filterScriptDir(ScriptType[], (helper) => filter)`: Set the filter for script directories, files in denied directories will also be denied
- `filterResourceFile(PackType[], (helper) => filter)`: Set the filter for resource files
- `filterResourceDir(PackType[], (helper) => filter)`: Set the filter for resource files, files in denied directories will also be denied
