#!/usr/bin/env python3
import unittest
from src.crowd_classifier import classify


class TestCrowdClassifier(unittest.TestCase):
    def test_low(self):
        self.assertEqual(classify(0), "Low")
        self.assertEqual(classify(39), "Low")

    def test_moderate(self):
        self.assertEqual(classify(40), "Moderate")
        self.assertEqual(classify(64), "Moderate")

    def test_high(self):
        self.assertEqual(classify(65), "High")
        self.assertEqual(classify(84), "High")

    def test_critical(self):
        self.assertEqual(classify(85), "Critical")
        self.assertEqual(classify(94), "Critical")
        self.assertEqual(classify(100), "Critical")

    def test_unknown(self):
        self.assertEqual(classify(None), "Unknown")


if __name__ == "__main__":
    unittest.main()
