//
// Created by admin on 2021-03-22.
//

#ifndef OPENGL_FACETRACK_H
#define OPENGL_FACETRACK_H

#include "include/seeta/FaceLandmarker.h"
#include "include/opencv2/opencv.hpp"
#include "include/opencv2/imgproc/types_c.h"
#include "include/opencv2/core/mat.hpp"


using namespace cv;
using namespace seeta;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {

        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);

    }

    virtual ~CascadeDetectorAdapter() {

    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<cv::CascadeClassifier> Detector;
};


class FaceTrack {
public:
    FaceTrack(const char *faceModel, const char *landmarkerModel);

    ~FaceTrack();

    void stop();

    void run();

    void process(Mat src, cv::Rect &face, std::vector<SeetaPointF> &points);

private:
    DetectionBasedTracker *tracker;
    FaceLandmarker *landmarker;
};

#endif //OPENGL_FACETRACK_H
