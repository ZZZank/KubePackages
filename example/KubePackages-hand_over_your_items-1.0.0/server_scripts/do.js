// ignored: false

const debugMode = KubePackages.isLoaded('debug_trigger')
// const debugMode = true
if (debugMode) {
    console.warn(`Debug mode for HandOverYourItems is enabled, items will be gave back to the original player.`)
}

ItemEvents.entityInteracted((event) => {
    /** @type {{player:Internal.Player}} */
    const { player } = event;
    if (!player.player || !player.crouching || player.fake) {
        return;
    }

    /** @type {Internal.Player} */
    const target = debugMode ? player : event.target;
    const item = player.mainHandItem
    if (item.empty || !target.player) {
        return;
    }

    let itemName = item.displayName;
    if (item.count > 1) {
        itemName = Text.of(item.count + "*").white().append(itemName)
    }

    player.tell(
        Text.translate(
            "chat.hand_over_your_items.sent",
            itemName,
            Text.of(target.name).darkBlue()
        ).blue()
    );
    target.tell(
        Text.translate(
            "chat.hand_over_your_items.received",
            itemName,
            Text.of(player.name).green()
        ).darkGreen()
    );

    player.setMainHandItem("minecraft:air");
    target.give(item);

    // cancel original right-click operations
    event.cancel();
});
