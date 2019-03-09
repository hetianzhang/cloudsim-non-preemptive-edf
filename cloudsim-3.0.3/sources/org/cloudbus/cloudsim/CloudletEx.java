package org.cloudbus.cloudsim;
/**
 * This class extends the cloudlet with arrivaltime and deadline
 * 
 * @author tianzhangh
 *
 */
public class CloudletEx extends Cloudlet{
	
	private double arrivalTime;
	private double deadline;

	public CloudletEx(int cloudletId, double arrivaltime, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);
		// TODO Auto-generated constructor stub
		this.arrivalTime = arrivaltime;
	}
	
	public double getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(double arrivaltime) {
		this.arrivalTime = arrivaltime;
	}

	public double getDeadline() {
		return deadline;
	}

	public void setDeadline(double deadline) {
		this.deadline = deadline;
	}

}
