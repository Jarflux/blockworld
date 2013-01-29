uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;
varying vec2 texCoord;
varying float sunLightLevel;
varying float fireLightLevel;
varying float magicLightLevel;

void main(){
    texCoord = inTexCoord;
    sunLightLevel = inColor.x;
    fireLightLevel = inColor.y;
    magicLightLevel = inColor.z;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}