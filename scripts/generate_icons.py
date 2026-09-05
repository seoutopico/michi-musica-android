"""Rebuild Michi's vector sources and launcher PNGs. Requires Pillow, no network."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUTLINE = "M12,56 L12,8 Q14,0 22,10 L38,28 L62,28 L80,8 Q89,0 88,16 L88,56 L97,68 Q85,90 50,99 Q15,90 3,68 Z"
START = (12, 56)
COMMANDS = [('L',(12,8)),('Q',(14,0),(22,10)),('L',(38,28)),('L',(62,28)),('L',(80,8)),('Q',(89,0),(88,16)),('L',(88,56)),('L',(97,68)),('Q',(85,90),(50,99)),('Q',(15,90),(3,68)),('L',START)]

def write(path, text):
    path = ROOT / path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8')

def render_png(path, bg, ink):
    size = 2048
    image = Image.new('RGBA',(size,size),(0,0,0,0))
    draw = ImageDraw.Draw(image)
    if bg: draw.ellipse((0,0,size-1,size-1),fill=bg)
    def transform(p): return ((26+p[0]*.56)/108*size,(25+p[1]*.56)/108*size)
    points=[transform(START)]
    current=START
    for cmd in COMMANDS:
        end=cmd[-1]
        if cmd[0]=='Q':
            control=cmd[1]
            for step in range(1,49):
                t=step/48
                points.append(transform(tuple((1-t)**2*current[i]+2*(1-t)*t*control[i]+t*t*end[i] for i in (0,1))))
        else: points.append(transform(end))
        current=end
    stroke = 4.5*.56/108*size
    draw.line(points, fill=ink, width=round(stroke), joint='curve')
    for cx,cy in points:
        r=stroke/2
        draw.ellipse((cx-r,cy-r,cx+r,cy+r),fill=ink)
    for x in (31,69):
        cx,cy=transform((x,65)); r=4.5*.56/108*size
        draw.ellipse((cx-r,cy-r,cx+r,cy+r),fill=ink)
    target=ROOT/path; target.parent.mkdir(parents=True,exist_ok=True)
    image.resize((512,512),Image.Resampling.LANCZOS).save(target)

vector='''<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <group android:translateX="26" android:translateY="25" android:scaleX="0.56" android:scaleY="0.56">
        <path android:pathData="%s" android:fillColor="#00000000" android:strokeColor="%s" android:strokeWidth="4.5" android:strokeLineJoin="round" android:strokeLineCap="round"/>
        <path android:pathData="M26.5,65 a4.5,4.5 0,1 0,9 0 a4.5,4.5 0,1 0,-9 0 M64.5,65 a4.5,4.5 0,1 0,9 0 a4.5,4.5 0,1 0,-9 0" android:fillColor="%s"/>
    </group>
</vector>
'''
write('app/src/main/res/drawable/ic_launcher_foreground.xml', vector%(OUTLINE,'#FF88AC','#FF88AC'))
write('app/src/main/res/drawable/ic_launcher_monochrome.xml', vector%(OUTLINE,'#FFFFFF','#FFFFFF'))
write('app/src/main/res/drawable/ic_launcher_background.xml','<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle"><solid android:color="#090A1B"/></shape>\n')
for name in ('ic_launcher','ic_launcher_round'):
    for qualifier in ('mipmap-anydpi','mipmap-anydpi-v26','mipmap-anydpi-v33'):
        mono='\n    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>' if qualifier.endswith('v33') else ''
        write(f'app/src/main/res/{qualifier}/{name}.xml','<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n    <background android:drawable="@drawable/ic_launcher_background"/>\n    <foreground android:drawable="@drawable/ic_launcher_foreground"/>'+mono+'\n</adaptive-icon>\n')
for name,bg,ink in [('midnight','#090A1B','#FF88AC'),('rose','#FFFAF7','#A33764'),('outline',None,'#FF88AC')]:
    svg=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">'+(f'<circle cx="54" cy="54" r="54" fill="{bg}"/>' if bg else '')+f'<g transform="translate(26 25) scale(.56)" fill="none" stroke="{ink}" stroke-width="4.5" stroke-linejoin="round" stroke-linecap="round"><path d="{OUTLINE}"/></g><g transform="translate(26 25) scale(.56)" fill="{ink}"><circle cx="31" cy="65" r="4.5"/><circle cx="69" cy="65" r="4.5"/></g></svg>\n'
    write(f'branding/michi-{name}.svg',svg)
    render_png(f'branding/michi-{name}.png',bg,ink)
    target=ROOT/f'iconpack/src/main/res/drawable-nodpi/michi_{name}.png';target.parent.mkdir(parents=True,exist_ok=True)
    target.write_bytes((ROOT/f'branding/michi-{name}.png').read_bytes())
print('Generated Android vectors, SVGs and 512px Niagara assets.')
