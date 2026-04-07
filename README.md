# The Gauntlet

<div align="center">

A comprehensive all-in-one plugin for The Gauntlet, combining the best features from multiple community plugins.

**Supports both Normal and Corrupted versions**

</div>

---

## 📋 Table of Contents

- [Features](#-features)
  - [Core Gauntlet Features](#core-gauntlet-features)
  - [Run History & Performance Tracking](#run-history--performance-tracking)
  - [Live Maze Map](#live-maze-map)
  - [Run History Panel](#run-history-panel)
- [Credits & Attribution](#-credits--attribution)
- [License](#-license)
- [Contribute](#-contribute)

---

## ✨ Features

### Core Gauntlet Features
*Features from [the-gauntlet](https://github.com/LlemonDuck/the-gauntlet) by rdutta*

- Icons, outlines, and tile overlays for resource nodes
- Resource infobox counters
- Timer for preparation stage and total duration
- Colored outlines for NPCs and Hunllef
- Boss attack/prayer indicators

<div align="center">

<table>
  <tr>
    <td align="center"><img src="/images/resource.png" alt="Resource Overlays" width="200"/><br/><b>Resource Overlays</b></td>
    <td align="center"><img src="/images/minimap.png" alt="Minimap" width="200"/><br/><b>Minimap</b></td>
    <td align="center"><img src="/images/counter.png" alt="Counters" width="200"/><br/><b>Counters</b></td>
  </tr>
  <tr>
    <td align="center"><img src="/images/timer.png" alt="Timer" width="200"/><br/><b>Timer</b></td>
    <td align="center"><img src="/images/outline.png" alt="NPC Outlines" width="200"/><br/><b>NPC Outlines</b></td>
    <td></td>
  </tr>
</table>

</div>

### Run History & Performance Tracking
*Features from [RLCGPerformanceTracker](https://github.com/powerus117/RLCGPerformanceTracker) by powerus117*

- Detailed performance statistics and metrics tracking
- Interactive Chart.js graphs showing:
  - DPS given/taken over time with moving averages
  - Tick efficiency trends
  - Prayer accuracy metrics
  - Mistake tracking (wrong prayers, attack styles, tornado hits)
- Separate tracking for Normal and Corrupted Gauntlet
- Persistent run history saved locally

<div align="center">

![Gauntlet Performance Tracker Example](https://github.com/powerus117/RLCGPerformanceTracker/assets/31093294/fc563bbb-93b0-4172-bf89-d6f139c4184d)

*Screenshot from [powerus117/RLCGPerformanceTracker](https://github.com/powerus117/RLCGPerformanceTracker)*

</div>

### Live Maze Map
*Features from [gauntlet-map](https://github.com/StickySerum/gauntlet-map) by Tyler (StickySerum)*

- Real-time maze mapping as you explore
- Player position tracking
- Demi-boss location markers (with combat style indicators)
- Resource markers (fishing spots, grym leaves)
- Toggleable sidebar panel and on-screen overlay
- Customizable opacity and tile size
- Demi-boss entrance node highlighting

<div align="center">

<table>
  <tr>
    <td align="center"><img src="/images/Map.png" alt="Live Maze Map" width="300"/><br/><b>Live Maze Map</b></td>
    <td align="center"><img src="/images/Overlay.png" alt="Map Overlay" width="300"/><br/><b>Map Overlay</b></td>
  </tr>
  <tr>
    <td align="center"><img src="/images/Highlight.png" alt="Demi-boss Highlighting" width="300"/><br/><b>Demi-boss Highlighting</b></td>
    <td align="center"><img src="/images/Settings.png" alt="Map Settings" width="300"/><br/><b>Map Settings</b></td>
  </tr>
</table>

*Screenshots from [StickySerum/gauntlet-map](https://github.com/StickySerum/gauntlet-map/tree/main/screenshots)*

</div>

### Run History Panel
*New feature unique to this plugin*

- Compact sidebar panel showing recent runs and personal bests
- One-click HTML export with full performance reports
- CSV export for advanced data analysis
- Quick access to run statistics and trends

<div align="center">

<img src="/images/SideNavScreenShot.png" alt="Sidebar Panel" width="400"/>

**📊 Example Report:** [View Sample HTML Report](/images/Gauntlet%20Run%20History.pdf)  
*(Click "View Full Report" button in-game to generate)*

</div>

---

## 🙏 Credits & Attribution

This plugin combines code from three excellent RuneLite plugins:

<table>
<tr>
<td>

### The Gauntlet
**Original Author:** rdutta  
**Repository:** [LlemonDuck/the-gauntlet](https://github.com/LlemonDuck/the-gauntlet)  
**License:** BSD 2-Clause  
**Contribution:** Core plugin functionality, resource tracking, overlays, timers, NPC outlines

</td>
</tr>
<tr>
<td>

### Gauntlet Map
**Original Author:** Tyler (StickySerum)  
**Repository:** [StickySerum/gauntlet-map](https://github.com/StickySerum/gauntlet-map)  
**License:** BSD 2-Clause  
**Contribution:** Live maze mapping, demi-boss tracking, resource markers, map overlays

</td>
</tr>
<tr>
<td>

### RLCG Performance Tracker
**Original Author:** powerus117  
**Repository:** [powerus117/RLCGPerformanceTracker](https://github.com/powerus117/RLCGPerformanceTracker)  
**License:** BSD 2-Clause  
**Contribution:** Run history tracking, performance statistics, CSV/HTML export, Chart.js integration

</td>
</tr>
</table>

**All original code and assets remain under their respective BSD 2-Clause licenses.** This derivative work maintains the same license and properly attributes all original authors.

---

## 📄 License

BSD 2-Clause License - See [LICENSE](LICENSE) file for full text including all copyright holders.

---

## 🤝 Contribute

Feature requests and pull requests are welcome!
