package zank.mods.kube_packages.impl.dependency;

import net.minecraft.network.chat.Component;
import org.slf4j.event.Level;

import java.util.*;

/**
 * @author ZZZank
 */
public class DependencyReport {
    private final EnumMap<Level, List<Component>> reports = new EnumMap<>(Level.class);

    public List<Component> getReportsAt(Level level) {
        return reports.computeIfAbsent(level, ignored -> new ArrayList<>());
    }

    public Map<Level, List<Component>> viewAllReports() {
        return Collections.unmodifiableMap(this.reports);
    }

    public void addReport(Level level, Component report) {
        getReportsAt(level).add(Objects.requireNonNull(report));
    }

    public void addInfo(Component info) {
        addReport(Level.INFO, info);
    }

    public void addWarning(Component warning) {
        addReport(Level.WARN, warning);
    }

    public void addError(Component error) {
        addReport(Level.ERROR, error);
    }
}
