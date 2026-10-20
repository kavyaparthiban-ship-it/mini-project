# CrowdSense: Crowd Analysis & Evaluation

This module supports the optional Python-based crowd prediction and recommendation evaluation (Member 4). The Java backend works independently using its own crowd classification and scoring.

## Member
**Member 4 — Crowd Prediction and Recommendation Evaluation**

## Contents
- `data/historical_occupancy.csv` — Simulated historical occupancy samples
- `src/crowd_classifier.py` — Baseline rule-based classifier (mirrors Java thresholds)
- `src/evaluate_recommendations.py` — Evaluation script for recommendation quality
- `tests/test_crowd_classifier.py` — Unit tests for the Python classifier
- `results/evaluation_results.csv` — Generated evaluation output
- `README.md`

## Setup (Windows)
```powershell
# Create a virtual environment (recommended)
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
```

`requirements.txt`:
```text
pandas
numpy
scikit-learn
```
*(These are already installed in this environment.)*

## Run classifier
```powershell
cd crowd-analysis
python src\crowd_classifier.py
```

## Run evaluation
```powershell
cd crowd-analysis
python src\evaluate_recommendations.py
```

## Integration
The Java backend does **not** require this module to run. If a trained scikit-learn model is added later, it can be exposed via a small REST service or batch-exported; integration is documented here. No fabricated accuracy numbers are reported — metrics are only written when computed.
