package zank.mods.kube_packages.api;

import dev.latvian.mods.kubejs.script.*;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import zank.mods.kube_packages.api.inject.ScriptPackLoadHelper;

/**
 * @author ZZZank
 */
public class ScriptLoadContext {
    private final ScriptManager manager;
    private final String folderName;

    public ScriptLoadContext(ScriptManager manager) {
        this.manager = manager;
        this.folderName = folderName(type());
    }

    public ScriptType type() {
        return manager.scriptType;
    }

    public String folderName() {
        return folderName;
    }

    public static String folderName(ScriptType type) {
        return type.name + "_scripts";
    }

    public ScriptManager manager() {
        return manager;
    }

    public ConsoleJS console() {
        return type().console;
    }

    public void loadFileIntoPack(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source) {
        var packLoadHelper = (ScriptPackLoadHelper) manager;
        packLoadHelper.kpkg$loadFile(pack, fileInfo, source);
    }
}
