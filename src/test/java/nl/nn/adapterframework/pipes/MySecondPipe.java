package nl.nn.adapterframework.pipes;


import nl.nn.adapterframework.configuration.ConfigurationWarning;

public class MySecondPipe {
    private String myPipeAttribute;

    @Deprecated
    @ConfigurationWarning("Change to myPipeAttribute")
    public void setPipeAttribute(String pipeAttribute) {
        this.myPipeAttribute = pipeAttribute;
    }

    public void setMyPipeAttribute(String pipeAttribute) {
        this.myPipeAttribute = pipeAttribute;
    }
}
