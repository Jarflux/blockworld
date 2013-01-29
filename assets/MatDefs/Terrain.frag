uniform sampler2D m_Terrain;
uniform float m_DayAlpha;
uniform vec4 m_SunColor;
uniform vec4 m_MoonColor;
uniform vec4 m_FireColor;
uniform vec4 m_MagicColor;
varying vec2 texCoord;
varying float sunLightLevel;
varying float fireLightLevel;
varying float magicLightLevel;

void main(){
    vec4 color = texture2D( m_Terrain, texCoord );
    float xMax = color.x * color.x;
    float yMax = color.y * color.y;
    float zMax = color.z * color.z;
    vec4 black = vec4(0.0,0.0,0.0,1.0);

    vec4 sunLight = mix(black, vec4(color.x*m_SunColor.x, color.y*m_SunColor.y, color.z*m_SunColor.z, 1.0), sunLightLevel*m_DayAlpha);
    vec4 moonLight = mix(black, vec4(color.x*m_MoonColor.x, color.y*m_MoonColor.y, color.z*m_MoonColor.z, 1.0), sunLightLevel*(1-m_DayAlpha));
    vec4 naturalLight = vec4((sunLight.x+moonLight.x)/(1+(sunLight.x*moonLight.x/xMax)), (sunLight.y+moonLight.y)/(1+(sunLight.y*moonLight.y/yMax)), (sunLight.z+moonLight.z)/(1+(sunLight.z*moonLight.z/zMax)), 1.0);
    
    vec4 fireLight = mix(black, vec4(color.x*m_FireColor.x, color.y*m_FireColor.y, color.z*m_FireColor.z, 1.0), fireLightLevel);
    vec4 combinedLight = vec4( (naturalLight.x+fireLight.x)/(1+naturalLight.x*fireLight.x/xMax), (naturalLight.y+fireLight.y)/(1+naturalLight.y*fireLight.y/yMax), (naturalLight.z+fireLight.z)/(1+naturalLight.z*fireLight.z/zMax), 1.0 );
    
    vec4 magicLight = mix(black, vec4(color.x*m_MagicColor.x, color.y*m_MagicColor.y, color.z*m_MagicColor.z, 1.0), magicLightLevel);
    gl_FragColor = vec4( (combinedLight.x+magicLight.x)/(1+combinedLight.x*magicLight.x/xMax), (combinedLight.y+magicLight.y)/(1+combinedLight.y*magicLight.y/zMax), (combinedLight.z+magicLight.z)/(1+combinedLight.z*magicLight.z/zMax), 1.0 );
}