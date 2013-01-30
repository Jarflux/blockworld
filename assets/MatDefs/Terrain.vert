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
    redLightLevel = inColor.x;
    greenLightLevel = inColor.y;
    blueLightLevel = inColor.z;
    sunLightLevel = inColor.a;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}