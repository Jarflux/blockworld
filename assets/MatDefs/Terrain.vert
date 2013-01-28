uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;
varying vec2 texCoord;
varying float naturalLightLevel;
varying float artificialLightLevel;

void main(){
    texCoord = inTexCoord;
    naturalLightLevel = inColor.x;
    artificialLightLevel = inColor.y;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}