package classes;

public class TestStep {
    private String stepName;
    private String action;
    private String xpath;
    private String data;
    private double waitTime;

    public TestStep(String stepName, String action, String xpath, String data, double waitTime) {
        this.stepName = stepName;
        this.action = action;
        this.xpath = xpath;
        this.data = data;
        this.waitTime = waitTime;
    }

    public String getStepName() {
        return stepName;
    }

    public String getAction() {
        return action;
    }

    public String getXpath() {
        return xpath;
    }

    public String getData() {
        return data;
    }

    public double getWaitTime() {
        return waitTime;
    }
}
