# Smart Route Planner

A Java-based route optimization system for multi-vehicle routing with traffic considerations. Solves the Vehicle Routing Problem (VRP) by assigning nodes to multiple buses and optimizing their routes to minimize the maximum distance traveled.

## Features

- ✅ **Traffic-Aware Routing**: Considers time-dependent traffic multipliers
- ✅ **Multi-Vehicle Optimization**: Distributes workload across multiple buses
- ✅ **Load Balancing**: Ensures fair distribution of routes
- ✅ **Multiple Algorithms**: Greedy clustering, Nearest Neighbor TSP, 2-Opt optimization
- ✅ **Multiple Export Formats**: JSON, CSV, DOT (GraphViz), plain text
- ✅ **Interactive CLI and GUI**: User-friendly command-line interface
- ✅ **Real-time Visualization**: Watch routes being drawn on the map  
- ✅ **Interactive Legend**: Toggle specific bus routes on/off  
- ✅ **Multiple Algorithms**: Greedy clustering, Nearest Neighbor TSP, 2-Opt optimization  

## System Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Installation

```bash
git clone https://github.com/yourusername/smart-route-planner.git
cd smart-route-planner
mvn clean install
```

## Quick Start

### Running the CLI Application

```bash
mvn exec:java -Dexec.mainClass="com.example.srp.app.ConsoleApp"
```

The application will prompt you for:
1. **Map name**: e.g., `map-1`
2. **Start node**: e.g., `N1`
3. **Mandatory nodes**: e.g., `N2,N3,N4,N5`
4. **Number of buses**: e.g., `2`
5. **Hour of day**: e.g., `8` (for traffic multipliers)

### Example Input/Output

```
Enter start node ID: N1
Enter mandatory nodes (comma-separated): N2,N3,N4,N5
Enter number of buses: 2
Enter hour of day (0-23): 8

=== RESULTS ===

🚌 Bus 0:
   Route: [N1, N2, N3, N1]
   Distance: 5.50 km
   Nodes: 2

🚌 Bus 1:
   Route: [N1, N4, N5, N1]
   Distance: 5.60 km
   Nodes: 2

📈 Overall Metrics:
   Makespan (longest route): 5.60 km
   Total distance: 11.10 km
   Imbalance ratio: 1.02x
   Status: ✓ BALANCED
```

### Running the CLI Application

Method 1: Run via Maven

```bash
mvn exec:java -Dexec.mainClass="com.example.srp.ui.SRPApplication"  
```

Method 2; Build Executable JAR

```bash
mvn package  
java -jar target/Smart-Route-Planner-1.0-SNAPSHOT.jar  
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/srp/
│   │   ├── algorithms/
│   │   │   ├── pathfinding/       # Dijkstra, PathCache
│   │   │   ├── clustering/        # Node assignment algorithms
│   │   │   ├── routing/           # TSP solvers
│   │   │   ├── balancing/         # Load balancing
│   │   │   └── expansion/         # Route expansion
│   │   ├── models/                # Data models
│   │   ├── traffic/               # Traffic data handling
│   │   ├── io/                    # Input/Output utilities
│   │   └── app/                   # Main applications (CLI)  
│   │   └── ui/                    # Main applications (GUI)  
│   └── resources/
│       └── maps/                  # JSON map files
└── test/
    └── java/com/example/srp/      # Unit and integration tests
```

## Map File Format

Maps are defined in JSON format:

```json
{
  "name": "Sample Graph",
  "vertices": [
    { "id": "N1", "x": 100, "y": 80 },
    { "id": "N2", "x": 200, "y": 160 }
  ],
  "edges": [
    {
      "id": "E1",
      "from": "N1",
      "to": "N2",
      "distance": 2.4,
      "traffic": [1.0, 1.2, 1.5, ..., 1.0]
    }
  ]
}
```

- **vertices**: Nodes with IDs and coordinates
- **edges**: Roads with base distance and 24-hour traffic multipliers
- **traffic**: Array of 24 values (one per hour) indicating traffic multiplier

## Algorithms

### Phase A: Shortest Path
- **Dijkstra's Algorithm**: Finds shortest paths considering traffic
- **PathCache**: Stores pre-computed pairwise distances

### Phase B: Clustering
- **Greedy Balanced Assignment**: Assigns nodes to buses
- Balances distance minimization with workload equity

### Phase C: Route Optimization
- **Nearest Neighbor**: Fast greedy TSP heuristic
- **2-Opt**: Iterative improvement algorithm

### Phase D: Load Balancing
- **Imbalance Detection**: Calculates max/min ratio
- **Rebalancing**: Optionally redistributes nodes

### Phase E: Route Expansion
- **Detail Generation**: Expands waypoints to full paths
- **Export**: Multiple output formats

## How to Use  
  
**1. Load Map**:   
- Enter the name of your map file (e.g., test_map if you have test_map.json in resources).  

**2. Configure**:  
- Set the number of buses available.  
- Choose your starting Depot node.  
- Set the departure hour (affects traffic).  
- Check the boxes for all Mandatory Waypoints the fleet must visit.  

**3.Calculate**:   
- Click the green button.  

**4. View Results**:  
- The map will animate the optimal routes.
- Use the checkbox legend at the bottom-left to hide/show specific buses.
- View detailed statistics and turn-by-turn directions in the sidebar.

## Configuration

### Balance Weight (Clustering)
Controls trade-off between distance and workload balance:
- `0.0`: Optimize only for distance
- `0.5`: Balanced approach (default)
- `1.0`: Optimize heavily for balance

### Imbalance Threshold
Maximum acceptable imbalance ratio:
- `1.0`: Perfect balance required
- `1.3`: 30% imbalance acceptable (default)
- `2.0`: 100% imbalance acceptable

## Performance

- **Small graphs** (<20 nodes): <1 second
- **Medium graphs** (20-100 nodes): 1-5 seconds
- **Large graphs** (100-500 nodes): 5-30 seconds

## Troubleshooting

### Common Issues

**Problem**: `Map file not found`
- **Solution**: Ensure map JSON file is in `src/main/resources/maps/`

**Problem**: `No path found between nodes`
- **Solution**: Verify graph is connected, check edge definitions

**Problem**: `Imbalance ratio is infinity`
- **Solution**: Check that all buses have nodes assigned

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Submit a pull request

## License

MIT License - see LICENSE file for details

## Authors

- Mushfiq Iqbal - Backend
- Md. Sabbir Hossain - Frontend

## Acknowledgments

- Dijkstra's Algorithm (1956)
- Nearest Neighbor Heuristic for TSP
- 2-Opt Local Search (Croes, 1958)
- Greedy Clustering Algorithms