#include "stdafx.h"
#include "constants.h"
#include <mpi.h>
#include <algorithm>

Node recv_incoming_node(MPI_Status &status);

int main() {

	int rank, world_size;
	double mytime;   /*declare a variable to hold the time returned*/
	MPI_Status status;
	MPI_Request request = MPI_REQUEST_NULL;

	MPI_Init(NULL, NULL);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &world_size);

	mytime = MPI_Wtime();  /*get the time just before work to be timed*/

	/*
	* Split the input graph to parts (subgraphs)
	*/
	int *boundaries = new int[world_size];

	if (world_size > 1) {
		if (rank == 0) {
			cout << "Split the input graph to parts (subgraphs):" << endl;
			boundaries = partition_file(FILENAME, DELIMETER, world_size);
			cout << "done!" << endl;
			cout << "Each process reads its chunk..." << endl << endl;
		}
	
		MPI_Bcast(boundaries, world_size, MPI_INT, 0, MPI_COMM_WORLD);
	}

	/*
	* Each Processor reads its chunk
	*/
	map<int, Node> nodes;
	if (world_size == 1) {
		nodes = parse_file(FILENAME, DELIMETER, 1);
		cout << "process 0 starts computing..." << endl;
	}
	else {
		nodes = parse_file(CHUNK_PREFIX + to_string(rank) + ".txt", DELIMETER, 2);
		cout << "process " << rank << " starts computing..." << endl;
	}

	//Core of PATRIC

	/*
	* For each node v in the Vertex set Vi
	* calculate the triangles
	*/
	int triangles_sum = 0;
	int completion_counter = 0;
	int *send_buffer = nullptr;

	for (auto const &node : nodes) {

		int last_proc = -1; // reset last_proc

							// for each neighbors of the node
		for (int i = 0; i < node.second.neighbors.size(); ++i) {

			int n_id = node.second.neighbors[i];		

			//if we have one process all the nodes are in the same chunk
			if (world_size == 1) {
				//std::sort( (node.second.neighbors).begin(), (node.second.neighbors).end() );
				triangles_sum += intersectionCount(node.second.neighbors, nodes[n_id].neighbors);
			}

			else {
				//if the neighbor exists in Vi, then find the triangles
				if (nodes.find(n_id) != nodes.end()) {
					//2 neighbor nodes
					//if a 3rd one is neighbor of both of them, then a triangle exists
					triangles_sum += intersectionCount(node.second.neighbors, nodes[n_id].neighbors);
				}

				// if the neighbor does not exist in Vi, then send the node to the processor
				// who has the neighbor as a core node
				else {
					int proc = node_proc_owner(boundaries, world_size, n_id);

					//send to processor that holds the node if not sent already
					if (rank != proc && last_proc != proc) {

						//if there is a pending request, wait for it to finish
						//to avoid corrupting the buffer
						if (request != MPI_REQUEST_NULL) {
							MPI_Wait(&request, &status);
							free(send_buffer);
						}

						// now send the node data
						send_buffer = seriealizeNode(node.second);
						int buffer_size = (int)(node.second.neighbors.size() + 1);
						MPI_Isend(send_buffer, buffer_size, MPI_INT, proc, TAG_DATA, MPI_COMM_WORLD, &request);
						last_proc = proc;
					}
				}
			}
		}//endfor each neighbors of the node

		 //read all incoming data messages
		int flag = 1;
		while (flag) {
			MPI_Iprobe(MPI_ANY_SOURCE, TAG_DATA, MPI_COMM_WORLD, &flag, &status);
			if (flag) {
				Node n = recv_incoming_node(status);
				triangles_sum += surrogateCount(nodes, n);
			}
		}

	}//endfor each node v in the Vertex set Vi


	 /*
	 * broadcast message with notifier tag
	 */
	for (int i = 0; i < world_size; ++i) {
		//no reason to send data, just the TAG_NOTIFIER
		if (rank != i)
			MPI_Isend(NULL, 0, MPI_BYTE, i, TAG_NOTIFIER, MPI_COMM_WORLD, &request);
	}

	/*
	* wait for all the other processors to finish,
	* as they may send data for surrogate count
	*/
	int flag = 0;
	while (completion_counter < world_size - 1) {

		//check for notifier message from any processor
		MPI_Iprobe(MPI_ANY_SOURCE, TAG_NOTIFIER, MPI_COMM_WORLD, &flag, &status);
		if (flag) {
			MPI_Recv(NULL, 0, MPI_BYTE, MPI_ANY_SOURCE, TAG_NOTIFIER, MPI_COMM_WORLD, &status);
			completion_counter++;
		}

		//read all incoming data messages
		while (flag) {
			MPI_Iprobe(MPI_ANY_SOURCE, TAG_DATA, MPI_COMM_WORLD, &flag, &status);
			if (flag) {
				Node n = recv_incoming_node(status);
				triangles_sum += surrogateCount(nodes, n);
			}
		}
	}

	//Blocks until all processes in the communicator have reached this routine
	MPI_Barrier(MPI_COMM_WORLD);	


	/*
	* Reduce all of the local sums into the global sum
	*/
	int global_sum;
	MPI_Reduce(&triangles_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);

	mytime = MPI_Wtime() - mytime; /*get the time just after work is done
								   and take the difference */
	mytime = mytime / 60; //time in minutes

	// Print the result
	if (rank == 0) {
		cout << endl << "finished ..." << endl;
		cout << "Total time: " << mytime << " minutes" << endl;
		cout << endl << "TOTAL TRIANGLES=" << global_sum << endl;
	}

	MPI_Finalize();
	//cin.get();
	//system("pause");
	return 0;
}

Node recv_incoming_node(MPI_Status &status) {
	int count = 0;
	MPI_Get_count(&status, MPI_INT, &count);
	int *buffer = new int[count];
	MPI_Recv(buffer, count, MPI_INT, MPI_ANY_SOURCE, TAG_DATA, MPI_COMM_WORLD, &status);
	Node node = deseriealizeNode(buffer, count);
	free(buffer);
	return node;
}