package zank.mods.kube_packages.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.file.PathFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * a visitor that copies contents in a directory to another directory. Not using the one from Apache Commons IO because the one use by Minecraft is outdated and thus buggy
 *
 * @author ZZZank
 * @see org.apache.commons.io.file.CopyDirectoryVisitor
 */
@Getter
@Accessors(fluent = true)
public class DirCopyVisitor extends SimpleFileVisitor<Path> {

    private final Counters.PathCounters counters;
    private final PathFilter fileFilter;
    private final PathFilter dirFilter;
    private final Path sourceDirectory;
    private final Path targetDirectory;
    @Getter(AccessLevel.NONE) private final CopyOption[] copyOptions;

    public DirCopyVisitor(Path sourceDirectory, Path targetDirectory, CopyOption... copyOptions) {
        this(
            Counters.longPathCounters(),
            FileFilterUtils.trueFileFilter(),
            FileFilterUtils.trueFileFilter(),
            sourceDirectory,
            targetDirectory,
            copyOptions
        );
    }

    public DirCopyVisitor(
        Counters.PathCounters counters,
        PathFilter fileFilter,
        PathFilter dirFilter,
        Path sourceDirectory,
        Path targetDirectory,
        CopyOption... copyOptions
    ) {
        this.counters = Objects.requireNonNull(counters, "pathCounters == null");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter == null");
        this.dirFilter = Objects.requireNonNull(dirFilter, "directoryFilter == null");
        this.sourceDirectory = Objects.requireNonNull(sourceDirectory, "sourceDirectory == null");
        this.targetDirectory = Objects.requireNonNull(targetDirectory, "targetDirectory == null");
        this.copyOptions = copyOptions.clone();
    }

    @Override
    public @NotNull FileVisitResult preVisitDirectory(@NotNull Path directory, @NotNull BasicFileAttributes attributes)
        throws IOException {
        var newTargetDir = resolveInTarget(directory);

        if (Files.notExists(newTargetDir)) {
            Files.createDirectory(newTargetDir);
        }

        var accept = dirFilter.accept(directory, attributes);
        return accept == FileVisitResult.CONTINUE ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
    }

    private @NotNull Path resolveInTarget(@NotNull Path directory) {
        return FileUtil.resolve(targetDirectory, sourceDirectory.relativize(directory));
    }

    @Override
    public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
        updateDirCounter(dir, exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public @NotNull FileVisitResult visitFile(@NotNull Path sourceFile, @NotNull BasicFileAttributes attributes)
        throws IOException {
        var targetFile = resolveInTarget(sourceFile);

        // Note: A file can be a symbolic link to a directory.
        if (Files.exists(sourceFile) && fileFilter.accept(sourceFile, attributes) == FileVisitResult.CONTINUE) {
            Files.copy(sourceFile, targetFile, copyOptions);
            updateFileCounters(targetFile, attributes);
        }

        return FileVisitResult.CONTINUE;
    }

    protected void updateFileCounters(final Path file, final BasicFileAttributes attributes) {
        counters.getFileCounter().increment();
        counters.getByteCounter().add(attributes.size());
    }

    protected void updateDirCounter(final Path dir, final IOException exc) {
        counters.getDirectoryCounter().increment();
    }

    @Override
    public String toString() {
        return counters.toString();
    }
}
