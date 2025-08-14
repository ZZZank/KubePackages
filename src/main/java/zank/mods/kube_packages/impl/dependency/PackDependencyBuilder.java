package zank.mods.kube_packages.impl.dependency;

import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.api.meta.dependency.LoadOrdering;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class PackDependencyBuilder {

    public void build(Collection<SortableKubePackage> sortables) {
        var indexed = sortables.stream()
            .collect(Collectors.toMap(SortableKubePackage::id, Function.identity()));
        for (var sortable : sortables) {

            for (var dependency : sortable.pack().metadata().dependencies()) {
                if (dependency.type() != DependencyType.REQUIRED
                    && dependency.type() != DependencyType.OPTIONAL) {
                    continue;
                }

                var target = indexed.get(dependency.id());
                if (dependency.type() == DependencyType.OPTIONAL && target == null) {
                    continue;
                }

                switch (dependency.ordering().orElse(LoadOrdering.NONE)) {
                    case NONE -> {}
                    case AFTER -> sortable.getTopoDependencies().add(target);
                    case BEFORE -> target.getTopoDependencies().add(sortable);
                }
            }
        }
    }
}
