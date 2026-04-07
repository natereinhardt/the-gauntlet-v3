# Credits & Attribution

This project, **The Gauntlet**, is a derivative work that integrates code and assets from three excellent RuneLite community plugins. All original work remains under the BSD 2-Clause License, and this derivative maintains the same license while properly attributing all contributors.

---

## Original Projects

### 1. The Gauntlet
**Original Repository:** https://github.com/LlemonDuck/the-gauntlet  
**Author:** rdutta  
**License:** BSD 2-Clause License  
**Copyright:** (c) 2023, rdutta

**What was used:**
- Core plugin architecture and module system
- Resource tracking (ore, bark, fish, crystal shards, etc.)
- Resource overlay and infobox counters
- Timer system (preparation time, boss time, total time)
- NPC and Hunllef overlays
- Boss attack and prayer indicators
- Configuration system
- All original visual assets (icons, overlays)

**Files incorporating this work:**
- `src/main/java/nr/gauntlet/` (all core modules except `map/` and `history/`)
- `src/main/resources/nr/gauntlet/` (icons, images - excluding `module/map/`)

---

### 2. Gauntlet Map
**Original Repository:** https://github.com/StickySerum/gauntlet-map  
**Author:** Tyler (StickySerum)  
**License:** BSD 2-Clause License  
**Copyright:** (c) 2023, Tyler

**What was used:**
- Live maze mapping system
- Real-time player position tracking
- Demi-boss location detection and combat style indicators
- Resource node markers (fishing spots, grym leaves)
- Map panel UI (7x7 grid display)
- Map overlay system with customizable opacity
- Demi-boss entrance node highlighting
- Complete map tile graphics (60+ PNG assets)

**Files incorporating this work:**
- `src/main/java/nr/gauntlet/module/map/MapModule.java`
- `src/main/java/nr/gauntlet/module/map/MapSession.java`
- `src/main/java/nr/gauntlet/module/map/MapPanel.java`
- `src/main/java/nr/gauntlet/module/map/MapOverlay.java`
- `src/main/java/nr/gauntlet/module/map/DemiBossOverlay.java`
- `src/main/resources/nr/gauntlet/module/map/` (all map tiles and graphics)

---

### 3. RLCG Performance Tracker
**Original Repository:** https://github.com/powerus117/RLCGPerformanceTracker  
**Author:** powerus117  
**License:** BSD 2-Clause License  
**Copyright:** (c) 2024, powerus117

**What was used:**
- Run history tracking system
- Performance statistics (prep time, boss time, deaths)
- CSV export functionality
- HTML report generation with Chart.js graphs
- Moving average calculations
- Separate Normal/Corrupted Gauntlet tracking
- Historical data persistence

**Files incorporating this work:**
- `src/main/java/nr/gauntlet/module/history/RunHistoryManager.java`
- `src/main/java/nr/gauntlet/module/history/HistoryPanel.java`
- `src/main/java/nr/gauntlet/module/history/StatsTracker.java`
- Configuration entries related to run history

---

## Integration & Modifications

**Integration performed by:** Cascade AI Assistant  
**Date:** March 2026  
**Integration work includes:**
- Merging three separate plugins into unified codebase
- Package restructuring from `com.gauntletmap` and original packages to `nr.gauntlet.module.map`
- Configuration system unification
- Module lifecycle integration
- Map toggle functionality
- Resource path updates
- Dependency injection adjustments
- Bug fixes for compilation and compatibility

All integration work respects the original BSD 2-Clause licenses and maintains proper attribution.

---

## License Compliance

All three original projects use the **BSD 2-Clause License**, which permits:
- ✅ Use in source and binary forms
- ✅ Modification
- ✅ Redistribution
- ✅ Commercial use

**Requirements:**
1. ✅ Retain copyright notices (maintained in all source files)
2. ✅ Retain license text (included in LICENSE file)
3. ✅ Include disclaimer in documentation

**This derivative work complies with all requirements by:**
- Maintaining original copyright headers in source files
- Adding attribution to integrated files
- Including all copyrights in LICENSE file
- Creating this CREDITS.md for transparency
- Updating README.md with full attribution
- Keeping the same BSD 2-Clause License

---

## Full Copyright Statement

```
BSD 2-Clause License

This project incorporates code from multiple sources:

Copyright (c) 2023, rdutta (The Gauntlet plugin)
  https://github.com/LlemonDuck/the-gauntlet
  
Copyright (c) 2023, Tyler (StickySerum) (Gauntlet Map)
  https://github.com/StickySerum/gauntlet-map
  
Copyright (c) 2024, powerus117 (RLCG Performance Tracker)
  https://github.com/powerus117/RLCGPerformanceTracker

All rights reserved.
```

See [LICENSE](LICENSE) for full license text.

---

## Thank You

Special thanks to:
- **rdutta** for creating the comprehensive original Gauntlet plugin
- **Tyler (StickySerum)** for the excellent maze mapping functionality
- **powerus117** for the performance tracking and statistics system
- The RuneLite community for supporting open-source development

Without their excellent work, this plugin would not be possible! 🎉
