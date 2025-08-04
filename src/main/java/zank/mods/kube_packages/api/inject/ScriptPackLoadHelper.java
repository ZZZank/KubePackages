package zank.mods.kube_packages.api.inject;

import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptSource;

/**
 * @author ZZZank
 */
public interface ScriptPackLoadHelper {

    void kpkg$loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source);
}
