"""Collect the pinned upstream source archive and notices beside public APKs."""
import hashlib
import json
from pathlib import Path
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'dist' / 'sources'
OUT.mkdir(parents=True, exist_ok=True)
revision = 'd725d5c9a18c3a99a13ee0308bf78275dc310760'
sources = {
    'youtubedl-android-0.18.1.tar.gz': f'https://codeload.github.com/yausername/youtubedl-android/tar.gz/{revision}',
}
expected = {'youtubedl-android-0.18.1.tar.gz': 'cf3b5bc27da8ed34bb562c3f6ba8c758721291f2acbddfec698b5070c6c496f3'}
records=[]
for name,url in sources.items():
    dest=OUT/name
    if not dest.exists():
        pending=dest.with_suffix(dest.suffix+'.part')
        with urllib.request.urlopen(url,timeout=120) as response, pending.open('wb') as target:
            while block:=response.read(1024*1024): target.write(block)
        with pending.open('rb') as source:
            if hashlib.file_digest(source,'sha256').hexdigest() != expected[name]:
                raise ValueError(f'Unexpected source checksum: {name}')
        pending.replace(dest)
    with dest.open('rb') as source:
        digest=hashlib.file_digest(source,'sha256').hexdigest()
    if digest != expected[name]: raise ValueError(f'Unexpected source checksum: {name}')
    records.append(dict(file=name,url=url,sha256=digest))
for name,remote in [('LICENSE','LICENSE'),('docs/licenses/YOUTUBEDL_BUILD_FFMPEG.md','BUILD_FFMPEG.md'),('docs/licenses/YOUTUBEDL_BUILD_PYTHON.md','BUILD_PYTHON.md')]:
    dest=ROOT/name;dest.parent.mkdir(parents=True,exist_ok=True)
    with urllib.request.urlopen(f'https://raw.githubusercontent.com/yausername/youtubedl-android/{revision}/{remote}',timeout=30) as response:
        dest.write_bytes(response.read())
(OUT/'sources.json').write_text(json.dumps(records,indent=2)+'\n',encoding='utf-8')
print(json.dumps(records,indent=2))
