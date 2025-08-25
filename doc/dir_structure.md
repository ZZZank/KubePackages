
# KubePackage Directory Structure

[Example: debug_trigger](example/KubePackages-debug_trigger-1.0.0)

[Example: hand_over_your_items](example/KubePackages-hand_over_your_items-1.0.0)

These file formats can be the container of KubePackage:
- Directory
- `.zip` file
- Mod file

All 3 formats share the same directory structure:

```
[root]/
    ├── server_scripts/    # Server-side scripts (optional)
    ├── client_scripts/    # Client-side scripts (optional)
    ├── startup_scripts/   # Startup scripts (optional)
    ├── assets/            # Resource files (optional)
    ├── data/              # Data files (optional)
    └── kube_package.json  # Metadata file (required)
```

## 1. Folder

```
kube_packages
    └── packages
        └── [Folder Name]
            └── ... (directory structure described above)
```

## 2. ZIP Archive

```
kube_packages
    └── packages
        └── [File Name].zip
            └── ... (directory structure described above)
```

## 3. Mod Jar

```
mods
    └── [File Name].jar
        └── ... (directory structure described above)
```
