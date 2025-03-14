package nl.nn.adapterframework.pipes;


import nl.nn.adapterframework.configuration.ConfigurationWarning;

public class MySecondPipe {

    @Deprecated
    @ConfigurationWarning("Change to myPipeAttribute")
    public void setPipeAttribute(String pipeAttribute) {
        setMyPipeAttribute(pipeAttribute);
    }

    public void setMyPipeAttribute(String pipeAttribute) {
//        this.myPipeAttribute = pipeAttribute;
    }
}
