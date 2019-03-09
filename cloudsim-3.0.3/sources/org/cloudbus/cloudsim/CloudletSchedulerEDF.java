package org.cloudbus.cloudsim;
/**
 * A Non-preemptive real-time scheduler
 * 
 * @author TianZhang He
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

public class CloudletSchedulerEDF extends CloudletSchedulerSpaceShared{
	
	public CloudletSchedulerEDF() {
		super();
	}
	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cloudlet the submited cloudlet
	 * @param fileTransferTime time required to move the required files from the SAN to the VM
	 * @return expected finish time of this cloudlet, or 0 if it is in the waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		
		// it can go to the exec list
		if ((currentCpus - usedPes) >= cloudlet.getNumberOfPes()) {
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			rcl.setCloudletStatus(Cloudlet.INEXEC);
			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}
			getCloudletExecList().add(rcl);
			usedPes += cloudlet.getNumberOfPes();
		} else {// no enough free PEs: go to the waiting queue based on its deadline
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			getCloudletWaitingList().add(rcl);
			
			List<ResCloudlet> waitlist = getCloudletWaitingList();
			Collections.sort(waitlist, new Comparator<ResCloudlet>() {
			    @Override
			    public int compare(ResCloudlet lhs, ResCloudlet rhs) {
			        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
			        return lhs.getDeadline() > rhs.getDeadline() ? 1 : (lhs.getDeadline() < rhs.getDeadline()) ? -1 : 0;
			    }
			});
			cloudletWaitingList(waitlist);
			return 0.0;
		}

		// calculate the expected time for cloudlet completion
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : getCurrentMipsShare()) {
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}

		currentCpus = cpus;
		capacity /= cpus;

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = cloudlet.getCloudletLength();
		length += extraSize;
		cloudlet.setCloudletLength(length);
		return cloudlet.getCloudletLength() / capacity;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.CloudletScheduler#cloudletSubmit(cloudsim.Cloudlet)
	 */
	@Override
	public double cloudletSubmit(Cloudlet cloudlet) {
		return cloudletSubmit(cloudlet, 0.0);
	}
	/**
	 * Check if the taskset is schedulable with the new cloudlet
	 * @param cloudlet
	 * @return
	 */
	public <T extends Cloudlet> boolean isSchedulable(T cloudlet) {
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : getCurrentMipsShare()) {
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}

		currentCpus = cpus;
		capacity /= cpus;
		double nextEvent = Double.MAX_VALUE;
		double totalexpected = CloudSim.clock();
		for (ResCloudlet rcl : getCloudletExecList()) {
			double remainingLength = rcl.getRemainingCloudletLength();
			double estimatedFinishTime = (remainingLength / (capacity * rcl.getNumberOfPes()));
			if (estimatedFinishTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = CloudSim.getMinTimeBetweenEvents();
			}
			if (estimatedFinishTime < nextEvent) {
				totalexpected += estimatedFinishTime;
			}
		}
		
		List<ResCloudlet> waitinglist = new ArrayList<>();
		waitinglist.addAll(getCloudletWaitingList());
		
		waitinglist.add(new ResCloudlet((CloudletEx)cloudlet));
		Collections.sort(waitinglist, new Comparator<ResCloudlet>() {
		    @Override
		    public int compare(ResCloudlet lhs, ResCloudlet rhs) {
		        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
		        return ((CloudletEx) lhs.getCloudlet()).getDeadline() > ((CloudletEx) rhs.getCloudlet()).getDeadline() ? 
		        		1 : ((CloudletEx) lhs.getCloudlet()).getDeadline() < ((CloudletEx) rhs.getCloudlet()).getDeadline() ? -1 : 0;
		    }
		});
		
		for(ResCloudlet rcl : waitinglist) {
			double remainingLength = rcl.getRemainingCloudletLength();
			if(rcl.getCloudletId() == cloudlet.getCloudletId()) {
				remainingLength = cloudlet.getCloudletTotalLength();
			}else if(remainingLength == 0){
				remainingLength = rcl.getCloudletLength();
			}
				
			double estimatedFinishTime = (remainingLength / (capacity * rcl.getNumberOfPes()));
			if (estimatedFinishTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = CloudSim.getMinTimeBetweenEvents();
			}
			if (estimatedFinishTime < nextEvent) {
				totalexpected += estimatedFinishTime;
			}
			if (totalexpected > ((CloudletEx)rcl.getCloudlet()).getDeadline()) {
				return false;
			}
		}
		
		return true;
	}

}
