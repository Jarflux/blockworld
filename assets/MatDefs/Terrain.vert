uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;
varying vec2 texCoord;
varying float sunLightLevel;
varying float redLightLevel;
varying float greenLightLevel;
varying float blueLightLevel;

void main(){
    texCoord = inTexCoord;
    //float colorLightLevel = inColor.x;
    //blueLightLevel = colorLightLevel%(256*256);
    //greenLightLevel = (colorLightLevel-blueLightLevel)%256;
    //redLightLevel = colorLightLevel-greenLightLevel;  

    redLightLevel = inColor.x;    
    greenLightLevel = inColor.y;
    blueLightLevel = inColor.z;
    sunLightLevel = inColor.a;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}