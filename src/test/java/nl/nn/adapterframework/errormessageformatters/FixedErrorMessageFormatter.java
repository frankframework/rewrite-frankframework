//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package nl.nn.adapterframework.errormessageformatters;

import nl.nn.adapterframework.configuration.ConfigurationWarning;

public class FixedErrorMessageFormatter {
    private String filename = null;

    public FixedErrorMessageFormatter() {
    }
    /** @deprecated */
    @Deprecated
    @ConfigurationWarning("attribute 'fileName' is replaced with 'filename'")
    public void setFileName(String fileName) {
        this.setFilename(fileName);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
