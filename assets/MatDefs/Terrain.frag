uniform sampler2D m_Terrain;
uniform float m_DayAlpha;
uniform vec4 m_SunColor;
uniform vec4 m_MoonColor;
uniform vec4 m_RedColor;
uniform vec4 m_GreenColor;
uniform vec4 m_BlueColor;
varying vec2 texCoord;
varying float sunLightLevel;
varying float redLightLevel;
varying float greenLightLevel;
varying float blueLightLevel;

void main(){
    vec4 color = texture2D( m_Terrain, texCoord );
    float xMax = color.x * color.x;
    float yMax = color.y * color.y;
    float zMax = color.z * color.z;
    vec4 black = vec4(0.0,0.0,0.0,1.0);

    vec4 sunLight = mix(black, vec4(color.x*m_SunColor.x, color.y*m_SunColor.y, color.z*m_SunColor.z, 1.0), sunLightLevel*m_DayAlpha);
    vec4 moonLight = mix(black, vec4(color.x*m_MoonColor.x, color.y*m_MoonColor.y, color.z*m_MoonColor.z, 1.0), sunLightLevel*(1-m_DayAlpha));
    vec4 naturalLight = vec4((sunLight.x+moonLight.x)/(1+(sunLight.x*moonLight.x/xMax)), (sunLight.y+moonLight.y)/(1+(sunLight.y*moonLight.y/yMax)), (sunLight.z+moonLight.z)/(1+(sunLight.z*moonLight.z/zMax)), 1.0);
    
    vec4 redLight = mix(black, vec4(color.x*m_RedColor.x, color.y*m_RedColor.y, color.z*m_RedColor.z, 1.0), redLightLevel);
    vec4 combinedLight = vec4( (naturalLight.x+redLight.x)/(1+naturalLight.x*redLight.x/xMax), (naturalLight.y+redLight.y)/(1+naturalLight.y*redLight.y/yMax), (naturalLight.z+redLight.z)/(1+naturalLight.z*redLight.z/zMax), 1.0 );
    
    vec4 greenLight = mix(black, vec4(color.x*m_GreenColor.x, color.y*m_GreenColor.y, color.z*m_GreenColor.z, 1.0), greenLightLevel);
    combinedLight = vec4((combinedLight.x+greenLight.x)/(1+combinedLight.x*greenLight.x/xMax), (combinedLight.y+greenLight.y)/(1+combinedLight.y*greenLight.y/yMax), (combinedLight.z+greenLight.z)/(1+combinedLight.z*greenLight.z/zMax), 1.0 );
    
    vec4 blueLight = mix(black, vec4(color.x*m_BlueColor.x, color.y*m_BlueColor.y, color.z*m_BlueColor.z, 1.0), blueLightLevel);
    gl_FragColor = vec4((combinedLight.x+blueLight.x)/(1+combinedLight.x*blueLight.x/xMax), (combinedLight.y+blueLight.y)/(1+combinedLight.y*blueLight.y/zMax), (combinedLight.z+blueLight.z)/(1+combinedLight.z*blueLight.z/zMax), 1.0 );
}