#!/usr/bin/env python3
"""
CrowdSense - Crowd Classifier (Baseline)
========================================
Member 4: Crowd Prediction and Recommendation Evaluation

This baseline classifier mirrors the Java implementation exactly:
  Critical >= 85
  High      >= 65
  Moderate  >= 40
  else      Low

A future iteration may use scikit-learn (regression/classification) trained on
historical_occupancy.csv.  This script remains the verifiable baseline.
"""

import csv
from pathlib import Path

MODERATE_MIN = 40.0
HIGH_MIN = 65.0
CRITICAL_MIN = 85.0


def classify(occupancy_percent):
    if occupancy_percent is None:
        return "Unknown"
    try:
        pct = float(occupancy_percent)
    except (TypeError, ValueError):
        return "Unknown"
    if pct < 0 or pct > 100:
        raise ValueError("occupancy_percent must be between 0 and 100")
    if pct >= CRITICAL_MIN:
        return "Critical"
    if pct >= HIGH_MIN:
        return "High"
    if pct >= MODERATE_MIN:
        return "Moderate"
    return "Low"


def classify_from_csv(path):
    results = []
    path = Path(path)
    with path.open() as f:
        reader = csv.DictReader(f)
        for row in reader:
            pct = row.get("occupancy_percent")
            cat = classify(pct)
            results.append({
                "bus_number": row.get("bus_number"),
                "occupancy_percent": pct,
                "crowd_category": cat,
            })
    return results


if __name__ == "__main__":
    data_path = Path(__file__).parent.parent / "data" / "historical_occupancy.csv"
    for item in classify_from_csv(data_path):
        print(f"{item['bus_number']}: {item['occupancy_percent']}% -> {item['crowd_category']}")
