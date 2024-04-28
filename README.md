# RaytracerAnimation
RaytracerAnimation takes in multiple txt file listing a 3d scene as keyframes with spheres and raytracer lighting. These are then used to interpolate changes and generate all the images in a ppm form

## Setup
1. Install the Jama package and place it in your directory with the Java files (Ensure the folder is named Jama)
   * 
2. Create a folder in the directory, this will be for your input files
3. In this folder, create a txt for each keyframe naming them \[1-n\].txt
   * see examples folder

## Run
1. python3 GenerateFrames.py \[input-directory\]

## Frame Input Format
* NEAR \[z position of near plane\]
* LEFT \[x position of left plane\]
* RIGHT \[x position of right plane\]
* BOTTOM \[y position of bottom plane\]
* TOP \[y position of top plane\]
* RES \[width in pixels\] \[height in pixels\]
* SPHERE \[name\] \[x position of sphere center\] \[y position of sphere center\] \[z position of sphere center\] \[scale of in x direction\] \[scale in y direction\] \[scale in z direction\] \[red\] \[green\] \[blue\] \[ambient coefficient\] \[diffuse coefficient\] \[specular coefficient\] \[reflection coefficient\] \[normal\]
* ... \[more spheres\]
* LIGHT \[name\] \[x position\] \[y position\] \[z position\] \[red\] \[green\] \[blue\]
* ...\[more lights\]
* BACK \[red\] \[green\] \[blue\]
* AMBIENT \[red\] \[green\] \[blue\]
* FRAME \[frame number\]
* OUTPUT \[name of output folder (only needed in first frame)\]

## Terms
* red/green/blue: the amount of that color from 0 to 1
* NEAR/LEFT/RIGHT/BOTTOM/TOP: position of planes where cropping occurs
* BACK: background color
* AMBIENT: ambient color

