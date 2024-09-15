import cv2
import numpy as np
import os
from os.path import dirname, join

def preprocess(image_path: str):
    img = cv2.imread(image_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    clahe = cv2.createCLAHE(clipLimit=2.5, tileGridSize=(8, 8))
    gray = clahe.apply(gray)
    blur = cv2.GaussianBlur(gray, (9, 9), 2)
    kernel = np.ones((5, 5), np.uint8)
    morph = cv2.morphologyEx(blur, cv2.MORPH_CLOSE, kernel)

    edges = cv2.Canny(morph, 50, 150)

    circles = cv2.HoughCircles(morph, cv2.HOUGH_GRADIENT, dp=1.2, minDist=20, param1=100, param2=40, minRadius=20,
                               maxRadius=50)

    mask = np.zeros_like(gray)
    if circles is not None:
        circles = np.round(circles[0, :]).astype("int")
        for (x, y, r) in circles:
            cv2.circle(mask, (x, y), r, 255, -1)

    num_tubes = len(circles) if circles is not None else 0

    result = cv2.bitwise_and(img, img, mask=mask)

    return result, num_tubes, mask


def main(image_path: str):
    # os.path.getsize(join(dirname(__file__), "test_file"))

    if image_path.startswith("file://"):
        image_path = image_path[7:]

    blur = preprocess(image_path)
    processed_image, num_tubes, mask = preprocess(image_path)

    cv2.imwrite(image_path, processed_image)

    return num_tubes