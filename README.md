# Analog Clock

An interactive Java application displaying a real-time analog clock with hour, minute, and second hands. Originally developed by Robert Vanderbei at Princeton University (1997).

## Overview

This application displays a classic analog clock face that updates in real-time, showing:
- Hour hand (yellow, short)
- Minute hand (green, medium)
- Second hand (black, long)

## Features

- **Real-time Display**: Clock updates every second
- **Smooth Hand Motion**: Hour and minute hands move smoothly between positions
- **Classic Design**: Red clock face with white interior and tick marks

## Requirements

- Java 14+ (for running the JAR)
- Java 14+ with `jpackage` (for building macOS app bundle)

## Building

```bash
# Build the JAR file
make jar

# Build macOS .app bundle (unsigned)
make app
```

## Running

```bash
# Run the JAR
make run

# Or run directly
java -jar dist/ClockSim.jar
```

## Project Structure

```
clock-sim/
├── clock/
│   └── Clock.java          # Main app and ClockCanvas
├── myutil/
│   ├── AppletFrame.java    # Applet-to-Frame wrapper
│   └── GL.java             # Graphics library utilities
├── Makefile                # Build configuration
└── .github/workflows/      # CI/CD configuration
```

## Visual Elements

- **Red ring**: Outer clock border
- **White face**: Clock background
- **Red tick marks**: Hour positions (12 marks)
- **Black tick marks**: Minute positions (60 marks)
- **Yellow hand**: Hour hand (triangular)
- **Green hand**: Minute hand (triangular)
- **Black hand**: Second hand (line)

## How It Works

The clock uses trigonometry to position the hands:
- Each hand's angle is calculated based on the current time
- Angles are measured from 12 o'clock position (π/2 radians)
- The hour hand moves 30° per hour plus smooth interpolation for minutes
- The minute hand moves 6° per minute plus smooth interpolation for seconds
- The second hand moves 6° per second

## License

Copyright 1997, Princeton University.
Based on original work by Robert Vanderbei.
