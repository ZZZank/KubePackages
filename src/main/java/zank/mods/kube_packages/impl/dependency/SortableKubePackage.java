package zank.mods.kube_packages.impl.dependency;

import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.utils.topo.TopoSortable;
import dev.latvian.mods.kubejs.script.ScriptPack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ZZZank
 */
public class SortableKubePackage implements TopoSortable<SortableKubePackage> {
    private final String id;
    private final KubePackage pack;
    private final List<ScriptPack> scriptPacks;
    private final List<SortableKubePackage> dependencies = new ArrayList<>();

    public SortableKubePackage(String id, KubePackage pack, Collection<ScriptPack> scriptPacks) {
        this.id = id;
        this.pack = pack;
        this.scriptPacks = List.copyOf(scriptPacks);
    }

    public SortableKubePackage(String id, KubePackage pack, ScriptPack scriptPack) {
        this(id, pack, List.of(scriptPack));
    }

    @Override
    public Collection<SortableKubePackage> getTopoDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "SortableContentPack[%s]".formatted(id);
    }

    public KubePackage pack() {
        return pack;
    }

    public String id() {
        return id;
    }

    public List<ScriptPack> scriptPacks() {
        return scriptPacks;
    }
}
