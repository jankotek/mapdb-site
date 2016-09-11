---
title: MapDB Update
layout: single
---

- 3.1
  - tuples
  - builders
  - statistics
  
- append store
  - inspired by RocksDB 
    - merges multiple levels at the same time (rocks only merges two levels)
    - small chunks, rocks has long running tasks
  - derived from work for IOHK for cryptocurrency
  - concurrent merges
  - tunable (IO speed versus CPU load)
  - sharded intervals
  
- skip lists
  - use data pump 
  - might be good alternative to btreemap
    - depends on store (memory manager) efficiency
    
    