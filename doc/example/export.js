/*
Use [ProbeJS](https://www.curseforge.com/minecraft/mc-mods/probejs)
or [ProbeJS Legacy](https://www.curseforge.com/minecraft/mc-mods/probejs-legacy) for typing support
*/


KubePackages.metadataBuilder()
    .id("example_pkg")
    .name("WhateverPackage")
    .version("2.4.8")
    .authors(["Zank"])
    .description(`Hello World!

        actually, i'm curious, about whether the text block in JS will retain starting space`)
    .buildAndPushToCache() // for exporting package via command, not recommended since it provides less control

let meta = KubePackages.metadataBuilder()
    .id("hand_over_your_items") // id, mandatory
    .version("1.0.0") // version, mandatory
    .name("HandOverYourItems") // name, optional
    .authors(["Zank"]) // authors, optional
    // license, optional
    .description(`Give another player the item in your hand by Shift+Right Clicking the player`) // description, optional
    .dependencies([
        KubePackages.dependencyBuilder()
            .id("debug_trigger")
            .type('optional')
            .versionRange('[1.0,)')
            .reason("Install this package to enable Debug Mode")
            .build()
    ]) // dependencies, optional
    .build()

KubePackages.packageExporter()
    .exportAs("zip") // zip -> zip file, mod -> jar file, dir -> contents in a folder
    .metadata(meta)

    .resourceTypes(['client_resources']) // optional, determine which resource folders(assets, data) to include
    .scriptTypes(['server']) // optional, determine which script folders(startup/server/client) to include

    // optional, filtering files and directories
    .filterScriptFile(['client', 'server'], helper => helper.fileNameNoneOf("jsconfig.json", 'example.js'))
    .filterResourceDir(['client_resources'], helper => helper.fileNameNoneOf("kubejs"))

    .run()// or .runAsync()

KubePackages.packageExporter()
    .exportAs('zip')
    .metadata(
        KubePackages.metadataBuilder()
            .id('debug_trigger')
            .name("DebugTrigger")
            .version('1.0.0')
            .description("Example package for demonstrating the usage of cross package reference")
            .build()
    )
    .resourceTypes([])
    .scriptTypes(['startup'])
    .filterScriptFile(['startup'], h => h.fileNameOneOf('kpkg_example.js'))
    .run()
