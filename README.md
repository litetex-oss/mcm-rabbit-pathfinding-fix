<!-- modrinth_exclude.start -->

[![Version](https://img.shields.io/modrinth/v/todo)](https://modrinth.com/mod/rabbit-pathfinding-fix)
[![Build](https://img.shields.io/github/actions/workflow/status/litetex-oss/mcm-rabbit-pathfinding-fix/check-build.yml?branch=dev)](https://github.com/litetex-oss/mcm-rabbit-pathfinding-fix/actions/workflows/check-build.yml?query=branch%3Adev)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=litetex-oss_mcm-rabbit-pathfinding-fix&metric=alert_status)](https://sonarcloud.io/dashboard?id=litetex-oss_mcm-rabbit-pathfinding-fix)

<!-- modrinth_exclude.end -->

# Rabbit Pathfinding Fix

This mod fixes rabbit pathfinding / [MC-150224](https://bugs.mojang.com/browse/MC-150224).

As of 1.20.1 there are multiple problems with rabbit pathfinding:

### Basic pathfinding
1. The calculation of the jump height/velocity is incorrect and poorly implemented.<br/>This results in too small jumps for climbing over a block.
2. Rabbits "stall" (no horizontal movement) during jumps - due to this they just jump upwards in the same place when trying to climb a block.<br/>This behavior is caused by ``RabbitMoveControl`` which only sets the (horizontal) speed correctly during movement and not while jumping.
3. Rabbits are stuck / try to wander around forever.
   * The root cause is that ``EntityNavigation`` sets it's timeouts based on movement speed.<br/>If the movement speed is 0 (this is the case when a rabbit/mob is "stuck"), the timeout is also 0... and if the timeout is 0 it's ignored and therefore it's executed forever (or until interrupted by something external like another goal).
   * Rabbits only have a single goal when idle: ``WanderAround(Far)``. Most other entities also use ``LookAroundGoal``.<br/> Thus the above mentioned infinite navigation is never stopped in favor of executing another goal like in most other mobs.
   * ``RabbitMoveControl#tick`` constantly updates the rabbits speed (``RabbitEntity#setSpeed``).<br/> While doing this it also indirectly executes ``moveControl#moveTo`` thus the rabbit always tries to reach it's last target even when it shouldn't do that.

### Eating carrot crops
1. Rabbits can't reach the crops and always stop one block short of them.<br/>This is due to selecting the incorrect distance from the crop block (it's ``1`` but should be ``0``).
2. Rabbits instantly eat the crop while still jumping.
3. The goal/behavior is nearly instantly aborted (after a few ticks) due to incorrect implementation of ``shouldContinue`` and ``isTargetPos`` methods which abort the goal when the rabbit selected a target crop block.


<!-- modrinth_exclude.start -->

## Installation
[Installation guide for the latest release](https://github.com/litetex-oss/mcm-rabbit-pathfinding-fix/releases/latest#Installation)

## Contributing
See the [contributing guide](./CONTRIBUTING.md) for detailed instructions on how to get started with our project.

<!-- modrinth_exclude.end -->
