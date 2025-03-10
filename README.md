
# Traffic Light Simulator with Java

A [Hyperskill](https://hyperskill.org/projects/288 "Hyperskill") study project. All of the relevant code is located at [Traffic Light Simulator with Java/task/src/traffic/Main.java](https://github.com/TrusTio/Hyperskill-TrafficLightSimulator/blob/master/Traffic%20Light%20Simulator%20with%20Java/task/src/traffic/Main.java "Traffic Light Simulator with Java/task/src/traffic/Main.java") as using different classes errors out when you try to use the "Check" button to see if the implementation meets the requirements.

The project itself also contains the rest of the files necessary to see and check the task inside IntelliJ IDEA IDE.

It has the follwing capabilities:
- Automated Road Control: Uses a circular queue to manage roads, opening one at a time and cycling through them at user-defined intervals.
- User Interaction: Allows users to add, remove, and monitor roads via a menu-based control panel.
- Dynamic Time Calculation: Updates road statuses in real time, adjusting remaining times when roads are added or removed.
Example: 
```java
Road "First" will be open for 1s.
Road "Second" will be closed for 1s.
Road "Third" will be closed for 9s.
Road "Fourth" will be closed for 17s.
```
- Edge Case Handling:
 - If the open road is deleted, the next road opens after its scheduled time.
 - A single remaining road stays open but continues counting down.
 - If all roads are deleted, a newly added road starts open.
- Real-Time Display: Continuously shows road statuses and prompts the user for actions.
