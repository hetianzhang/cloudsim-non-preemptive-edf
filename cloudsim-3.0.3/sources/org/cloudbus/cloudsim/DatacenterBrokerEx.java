package org.cloudbus.cloudsim;
/**
 * @author TianZhang He
 */
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

public class DatacenterBrokerEx extends DatacenterBroker{
	
	// the VM Creation History
	public class VmCreatedHistory{
		public double startTime;
		public double endTime;
		public int vmId;
		
		public VmCreatedHistory() {
			setVmId(0);
			setStartTime(0);
			setEndTime(0);
		}
		public double getStartTime() {
			return this.startTime;
		}
		public void setStartTime(double startTime) {
			this.startTime = startTime;
		}
		public double getEndTime() {
			return this.endTime;
		}
		public void setEndTime(double endTime) {
			this.endTime = endTime;
		}
		public int getVmId() {
			return this.vmId;
		}
		public void setVmId(int vmId) {
			this.vmId = vmId;
		}
	};
	
	/** The cloudlets submitted. */
	protected int cloudletReturned;
	protected int currentVmId;
	protected List<? extends Vm> vmModel;
	protected HashMap<Integer, VmCreatedHistory> vmHistory;
	
	public DatacenterBrokerEx(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
		setVmModel(new ArrayList<Vm>());
		this.setCloudletReturned(0);
		this.setCurrentVmId(0);
		setVmHistory(new HashMap<Integer, VmCreatedHistory>());
	}
	
	public void setVmHistory(HashMap<Integer, VmCreatedHistory> vmHistory) {
		this.vmHistory = vmHistory;
	}
	
	public HashMap<Integer, VmCreatedHistory> getVmHistory(){
		return this.vmHistory;
	}

	public void submitVmModel(List<? extends Vm> list) {
		getVmModel().addAll(list);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmModel() {
		return (List<T>) vmModel;
	}

	protected <T extends Vm> void setVmModel(List<T> vmModel) {
		this.vmModel = vmModel;
	}
	public void setCurrentVmId(int id) {
		this.currentVmId = id;
	}
	public int getCurrentVmId() {
		return this.currentVmId;
	}
	public void setCloudletReturned(int cloudletReturned) {
		this.cloudletReturned = cloudletReturned;
	}
	public int getCloudletReturned() {
		return this.cloudletReturned;
	}
	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer: processVmInfo (Time Shared)/processVmInfoEx (NP-EDF)
			case CloudSimTags.VM_BROKER_EVENT:
				processVmInfo(ev);
				//processVmInfoEx(ev);
				break;
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}
	
	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			processVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}
	/**
	 * get the Vm state first
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void processVmsInDatacenter(int datacenterId) {
		
		double delay;
		if(cloudletsSubmitted < this.getCloudletList().size()) {
			// next Cloudlet Submission Time
			CloudletEx cloudlet = (CloudletEx) this.getCloudletList().get(cloudletsSubmitted);
			delay = cloudlet.getArrivalTime()-CloudSim.clock();// absolute time to  relevant time 
		    String datacenterName = CloudSim.getEntityName(datacenterId);
		    
		    // check the Vm statue for next Cloudlet submission
		    // get current Vm ID where the Cloudlet submitted to
		    Vm vm = VmList.getById(this.getVmsCreatedList(), this.getCurrentVmId());
		    if(vm == null) {
		    	Vm model = this.getVmModel().get(0);
			    vm = new Vm(this.getCurrentVmId(), this.getId(), model.getMips(), model.getNumberOfPes(), model.getRam(),
				    model.getBw(), model.getSize(), model.getVmm(), new CloudletSchedulerTimeShared());
			    this.getVmList().add(getCurrentVmId(), vm);
		    }
		    
		    Log.printLine(CloudSim.clock() + ": " + getName() + ": Check state of the creating VM #" + this.getCurrentVmId()
				+ " in " + datacenterName);
		    send(datacenterId, delay, CloudSimTags.VM_BROKER_EVENT, vm);
		}
	}
	protected void processVmInfoEx(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		
		Vm vm = VmList.getById(getVmsCreatedList(), vmId);
		if(vm == null) {
			//If not exit, create it directly
			this.setCurrentVmId(getVmsRequested());
			createVmInDatacenter(datacenterId);
		}
		else {
			// for cloudlet non intercepted EDF task scheduling
			CloudletSchedulerEDF scheduler = (CloudletSchedulerEDF) vm.getCloudletScheduler();
			Cloudlet cloudlet = this.getCloudletList().get(cloudletsSubmitted);
			
		    if(!scheduler.isSchedulable(cloudlet)) {
		    	// the threshold of creating a new Vm
			    this.setCurrentVmId(getVmsRequested());
			    createVmInDatacenter(datacenterId);
		    }
		    else
		    {
		    	// submit the Cloudlet to this Vm
			    submitCloudlet(vmId);
			    // check the Vm state for next submitting Cloudlet
			    processVmsInDatacenter(datacenterId);
		    }
		}
		
	}
	/**
	 * process creating a new Vm or not
	 * @param ev
	 */
	protected void processVmInfo(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		//int tag = data[2];
		
		Vm vm = VmList.getById(getVmsCreatedList(), vmId);
		if(vm == null) {
			//If not exit, create it directly
			this.setCurrentVmId(getVmsRequested());
			createVmInDatacenter(datacenterId);
		}
		else {
			// get the number of running Cloudlets
			Log.printLine(vm.getCloudletScheduler().runningCloudlets()+
					"cloudlet in "+"vm #" + vmId+ "info at time: " + CloudSim.clock());
			
		    if(vm.getCloudletScheduler().runningCloudlets() >=3) {
		    	// the threshold of creating a new Vm
			    this.setCurrentVmId(getVmsRequested());
			    createVmInDatacenter(datacenterId);
		    }
		    else
		    {
		    	// submit the Cloudlet to this Vm
			    submitCloudlet(vmId);
			    // check the Vm state for next submitting Cloudlet
			    processVmsInDatacenter(datacenterId);
		    }
		}
	}
	/**
	 * create new Vm in DataCenter
	 * @param datacenterId
	 */
	protected void createVmInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = this.getVmsRequested();
		String datacenterName = CloudSim.getEntityName(datacenterId);
		Vm vm = VmList.getById(getVmList(), getCurrentVmId());
		//create a new vm according to the Vm module
		if(vm == null) {
			Vm model = this.getVmModel().get(0);
			vm = new Vm(this.getCurrentVmId(), this.getId(), model.getMips(), model.getNumberOfPes(), model.getRam(),
					model.getBw(), model.getSize(), model.getVmm(), new CloudletSchedulerTimeShared());
		    this.getVmList().add(getCurrentVmId(), vm);
		}
		
		// only create one Vm at a time
		if (!getVmsToDatacentersMap().containsKey(this.getCurrentVmId())) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + getCurrentVmId()
					+ " in " + datacenterName);
			sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
			requestedVms++;
		}

	    getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);		
	}
	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
			// record the creation state of the Vm
			VmCreatedHistory vmhistory = new VmCreatedHistory();
			vmhistory.setVmId(vmId);
			vmhistory.setStartTime(CloudSim.clock());
			this.getVmHistory().put(vmId, vmhistory);
			
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();
		
        // submit the cloudlet to the new created Vm
		submitCloudlet(vmId);
		// check the statue of Vm for next Cloudlet
		processVmsInDatacenter(datacenterId);
	}
	

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlet(int vmId) {
		Cloudlet cloudlet = this.getCloudletList().get(cloudletsSubmitted);
		Vm vm;
            // submit to the specific vm
		vm = VmList.getById(getVmsCreatedList(), vmId);
		
		if (vm == null) { // vm was not created
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
					+ cloudlet.getCloudletId() + ": bount VM not available");
		}else {

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
				+ cloudlet.getCloudletId() + " to VM #" + vm.getId()+"in center "+getVmsToDatacentersMap().get(vm.getId()));
		cloudlet.setVmId(vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
		cloudletsSubmitted++;
		getCloudletSubmittedList().add(cloudlet);
		}
	}
	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		int minrun = Integer.MAX_VALUE;
		int vmId = 0;
		DecimalFormat dft = new DecimalFormat("###.##");
		Log.printLine(dft.format(CloudSim.clock()) + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");

		cloudletReturned++;
		this.setCloudletReturned(cloudletReturned);
		// when the vm is idle, destroy it
		Vm vm = VmList.getById(this.getVmList(), cloudlet.vmId);
		if(vm.getCloudletScheduler().runningCloudlets() == 0 && this.getVmsCreatedList().size()>1 ) {
			
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
			Log.printLine( dft.format(CloudSim.clock()) +":VM #"+vm.getId()+ " destroied!");
			VmCreatedHistory vmhistory = this.getVmHistory().get(vm.getId());
			this.getVmHistory().remove(vm.getId());
			
			this.getVmsCreatedList().remove(vm);
			vmhistory.setEndTime(CloudSim.clock());
			this.getVmHistory().put(vm.getId(), vmhistory);
		}
		// find the vm with available processing ability
		for(Vm newVm: this.getVmsCreatedList()) {
			int running = newVm.getCloudletScheduler().runningCloudlets();
			if(running < minrun) {
				vmId = newVm.getId();			
				minrun = running;
			}
		}
		Log.printLine("Set Current Vm #"+vmId);
		this.setCurrentVmId(vmId);
		
		if(this.getCloudletList().size() == this.getCloudletReturned()) {
			int size = getCloudletList().size();
			int returednum = getCloudletReturned();
			Log.printLine("cloudlets send: "+size+" returned: "+returednum);
			clearDatacenters();
			finishExecution();
		}
	}
	/**
	 * Destroy the virtual machines running in datacenters and record the vm destroy time
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
			//record all destroy time of the rest Vms in the end
			VmCreatedHistory vmhistory = this.getVmHistory().get(vm.getId());
			this.getVmHistory().remove(vm.getId());
			vmhistory.setEndTime(CloudSim.clock());
			this.getVmHistory().put(vm.getId(), vmhistory);
		}

		getVmsCreatedList().clear();
	}

}
