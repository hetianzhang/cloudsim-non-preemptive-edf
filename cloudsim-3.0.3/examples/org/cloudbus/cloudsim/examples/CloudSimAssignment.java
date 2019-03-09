package org.cloudbus.cloudsim.examples;

/**
 * @author TianZhang He
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletEx;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBrokerEx;
import org.cloudbus.cloudsim.DatacenterBrokerEx.VmCreatedHistory;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.jfree.chart.JFreeChart;
import org.cloudbus.cloudsim.examples.HistogramPlot;;

/**
 * This example showing how to create
 * a datacenter with 500 hosts and run several
 * cloudlets (real-time tasks with different submission time and deadline) from a file on it.
 * A VM model is added to the new datacenter broker.
 * The new Datacenterbroker will decide which VM should run the cloudlet at its submission time.
 *  and in a non-preemptive scheduler the order in the VM queue the task will be inserted based.
 */

public class CloudSimAssignment {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;
	/**
	 * the submission time list
	 */
	
	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample3...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			//Third step: Create Broker
			DatacenterBrokerEx broker = createBroker();
			int brokerId = broker.getId();

			//Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			//VM description
			//TO DO: different vm module here to add according to the submitted tasks based on EC2 Amazon Price
			
			int ECU = 1000; // 1 ECU equals 1000 MIPS
			int mips = (int)(ECU * 3);
			long size = 10000; //image size (MB)
			int ram = 4026; //vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; //number of cpus
			String vmm = "Xen"; //VMM name

			//create VMs on different vm model
			Vm vm = new Vm(0, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			//add the VMs to the vmList
			vmlist.add(vm);

			//submit vm list to the broker
			broker.submitVmModel(vmlist);


			//Fifth step: Create two Cloudlets
			cloudletList = new ArrayList<Cloudlet>();

			//add Cloudlet from a file
			readCloudletList("workload.txt",broker.getId());

			//submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			//no bind between cloudlet and vm here, we need to decide it at the submission time of a cloudlet

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();


			// Final step: Print results when simulation is over
			List<CloudletEx> newList = broker.getCloudletReceivedList();
			HashMap<Integer, DatacenterBrokerEx.VmCreatedHistory> vmhistory = broker.getVmHistory();

			CloudSim.stopSimulation();

        	printCloudletList(newList);
        	//draw the result to Histogram
        	drawCloudletHistogram(newList);
        	//draw the result to layered bar
        	drawCloudletBar(newList);
        	//draw the vm histogram
        	
        	//draw the vm bar
        	drawVmsHistogram(vmhistory);
        	printVmsHistory(vmhistory)
;
			Log.printLine("CloudSim Assignment finished!");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		//    our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 10000;

		// 3. Create PEs and add these into a list.
		for(int i=0; i<8; i++) {
			peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		}
		

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int ram = 65000; //host memory (MB)
		long storage = 10000000; //host storage 10TB
		int bw = 10000; // doesn't matter in this assignment
		
		for(int hostId =0; hostId<500; hostId++) {
			hostList.add(
	    			new Host(
	    				hostId,
	    				new RamProvisionerSimple(ram),
	    				new BwProvisionerSimple(bw),
	    				storage,
	    				peList,
	    				new VmSchedulerSpaceShared(peList)
	    			)
	    		); // create 500 same hosts
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// create my own datacenter broker
	private static DatacenterBrokerEx createBroker(){

		DatacenterBrokerEx broker = null;
		try {
			broker = new DatacenterBrokerEx("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	/**
	 * Read the Cloudlet objects from a file
	 * @param file file of the cloudlets
	 * @return list lis of Cloudlets
	 */
	private static void readCloudletList(String filename, int brokerId){
		//List<Cloudlet> list = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    //StringBuilder sb = new StringBuilder();
		    String line = br.readLine();
		    //Cloudlet properties
		    int id = 0;
		    double arrival;
		    int pesNumber =1;
			long mis;
			long memory;
			long storage;
			double deadline;
			UtilizationModel utilizationModel = new UtilizationModelFull();
		    while (line != null) {
		    	String[] data = line.split(" ");
		    	arrival = new Double(data[0].trim());
		    	mis = new Long(data[1].trim());
		    	memory = new Long(data[2].trim());
		    	storage = new Long(data[3].trim());
		    	deadline = new Double(data[4].trim());
		    	
				CloudletEx cloudlet = new CloudletEx(id, arrival, mis, pesNumber, memory, storage, 
						utilizationModel, utilizationModel, utilizationModel);
				cloudlet.setDeadline(deadline);
				cloudlet.setUserId(brokerId);
				
				//Log.printLine(id+": scheduling Window: " + (deadline - arrival) +" execution time:"+ mis/1000 +" ecu need "+ ecuNeed(arrival, mis, deadline));
				cloudletList.add(cloudlet);
		    	
		    	line = br.readLine();
		    	id++;
		    }
		}catch(IOException e){
			
		}
	}
	/**
	 * just calculate the ecu number which a cloudlet need for test
	 * @param arrival
	 * @param mis
	 * @param deadline
	 * @return
	 */
	@SuppressWarnings("unused")
	private static int ecuNeed(double arrival, long mis, double deadline) {
		int ECU=0;
		double schduleWindow = deadline - arrival;
		for(int m =1; m<=8; m++) {
			if(mis/(m * 1000) <= schduleWindow) {
				ECU = m;
				break;
			}
		}
		return ECU;
	}
	/**
	 * draw the histogram for a vmlist
	 * @param vmList
	 */
	private static void drawVmsHistogram(HashMap<Integer, DatacenterBrokerEx.VmCreatedHistory> vmList) {
		List<DatacenterBrokerEx.VmCreatedHistory> vmData = new ArrayList<>();
		Entry<Integer, VmCreatedHistory> vmEntry;
		Iterator<Entry<Integer, DatacenterBrokerEx.VmCreatedHistory>> iterator = vmList.entrySet().iterator();
	
		while(iterator.hasNext()) {
			vmEntry = iterator.next();
			vmData.add(vmEntry.getValue());
		}
		
	}
	/**
	 * draw a bar chart for a vmlist
	 * @param vmList
	 */
	@SuppressWarnings("unused")
	private static void drawVmsBar(HashMap<Integer, DatacenterBrokerEx.VmCreatedHistory> vmList) {
		List<DatacenterBrokerEx.VmCreatedHistory> vmData = new ArrayList<>();
		Entry<Integer, VmCreatedHistory> vmEntry;
		Iterator<Entry<Integer, DatacenterBrokerEx.VmCreatedHistory>> iterator = vmList.entrySet().iterator();
	
		while(iterator.hasNext()) {
			vmEntry = iterator.next();
			vmData.add(vmEntry.getValue());
		}
		
		JFreeChart chart = LayeredBarPlot.createChart(LayeredBarPlot.createVmDataset(vmData));
		try {
			HistogramPlot.saveToFile("vmLayeredbar.png",chart.createBufferedImage(1500, 1500));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * print the created Vm history: vmid, create time and destroy time
	 * @param vmList
	 */
	private static void printVmsHistory(HashMap<Integer, DatacenterBrokerEx.VmCreatedHistory> vmList) {
		//int size = vmList.size();
		double totaltime =0;
		Entry<Integer, VmCreatedHistory> vmEntry;
		Iterator<Entry<Integer, DatacenterBrokerEx.VmCreatedHistory>> iterator = vmList.entrySet().iterator();
		String indent = "    ";
		Log.printLine();
		Log.printLine("----------- VM OUTPUT ---------");
		Log.printLine("VM ID" +indent+"Create Time"+indent+"Destory Time");
		DecimalFormat dft = new DecimalFormat("###.##");
		while(iterator.hasNext()) {
			vmEntry = iterator.next();
			vmEntry.getKey();
			Log.printLine(indent + vmEntry.getValue().getVmId()+ indent+dft.format(vmEntry.getValue().getStartTime())+indent+
					dft.format(vmEntry.getValue().getEndTime()) );
			totaltime += vmEntry.getValue().getEndTime() - vmEntry.getValue().getStartTime();
		}
		Log.printLine("vm total time: "+dft.format(totaltime));
		
	}
	/**
	 * draw cloudlet histogram based on response time
	 * @param list
	 */
	private static void drawCloudletHistogram(List<? extends Cloudlet> list) {
		JFreeChart chart = HistogramPlot.createChart(HistogramPlot.createCloudletDataset(list));
		try {
			HistogramPlot.saveToFile("taskHistogram.png",chart.createBufferedImage(500, 500));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * draw a bar chart for the cloudlets list
	 * @param list
	 */
	private static void drawCloudletBar(List<? extends Cloudlet> list) {
		JFreeChart chart = LayeredBarPlot.createChart(LayeredBarPlot.createCloudletDataset(list));
		try {
			HistogramPlot.saveToFile("taskLayeredbar.png",chart.createBufferedImage(3500, 1500));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Prints the Cloudlet objects
	 * @param newList  list of Cloudlets
	 */
	private static void printCloudletList(List<? extends Cloudlet> newList) {
		int size = newList.size();
		double totaltime =0;
		CloudletEx cloudlet;
		

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + "Response Time" + indent+ "Submission"+ indent + "Start Time" + indent + "Finish Time"+ indent + "Deadline");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = (CloudletEx) newList.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");
				totaltime += cloudlet.getActualCPUTime();
				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent +dft.format(cloudlet.getArrivalTime())+indent+indent+ dft.format(cloudlet.getExecStartTime())+
						indent + indent + dft.format(cloudlet.getFinishTime())+indent+indent+dft.format(cloudlet.getDeadline()));
			}
		}
		Log.printLine("total Response Time: "+ dft.format(totaltime));

	}
}

