# cloudsim-non-preemptive-edf
A CloudSim https://github.com/Cloudslab/cloudsim extension of a non-preemptive Earliest Deadline First (EDF) algorithm for VM task scheduler with the new datacenter broker. It will be a good start and reference for researchers and engineers who want to simulate and evaluate their algorithms and policies in datacenters.

# Non-preemptive EDF algorithm

* The task scheduler checks the current waiting and execution queues when a new task arrived to one host. It will allocate the new arrival task to the waiting queue based on the task's deadline.

# Deadline-Aware and Energy Efficent DataCenter Broker:

* The broker will assign the task to the VM which could successfully meet the task's deadline by searching current established VMs first. * The broker will estimate the execution time based on the availabile CPU time and waiting queue on each VMs.
* If there is no such VM that could assure the task meeting its deadline, a new VM will be create accordingly.
* The broker will also delete one VM when there is no task being executed and in the waiting queue to achieve a better economical scheme and energy effiency.

