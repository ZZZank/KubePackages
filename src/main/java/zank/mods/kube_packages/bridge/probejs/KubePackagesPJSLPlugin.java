package zank.mods.kube_packages.bridge.probejs;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

/**
 * @author ZZZank
 */
public class KubePackagesPJSLPlugin implements ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(ArtifactVersion.class, Types.STRING);
        scriptDump.assignType(VersionRange.class, Types.STRING);
    }
}
