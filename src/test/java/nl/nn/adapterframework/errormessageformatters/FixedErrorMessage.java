//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package nl.nn.adapterframework.errormessageformatters;

import nl.nn.adapterframework.configuration.ConfigurationWarning;

public class FixedErrorMessage {

    public FixedErrorMessage() {
        //empty constructor needed for test
    }
    /** @deprecated */
    @Deprecated
    @ConfigurationWarning("attribute 'fileName' is replaced with 'filename'")
    public void setFileName(String fileName) {
        this.setFilename(fileName);
    }

    public void setFilename(String filename) {
//        this.filename = filename;
    }
}
