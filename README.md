# RaytracerAnimation
RaytracerAnimation takes in multiple txt file listing a 3d scene as keyframes with spheres and raytracer lighting. These are then used to interpolate changes and generate all the images in a ppm form

## Setup
1. Install the Jama package and place it in your directory with the Java files (Ensure the folder is named Jama)
   * 
2. Create a folder in the directory, this will be for your input files
3. In this folder, create a txt for for each keyframe naming them \[1-n\].txt
   * see examples folder

## Run
1. python3 GenerateFrames.py \[output-directory\]
