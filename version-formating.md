# Version Format 📦

How versions will look after `0.9.5.3R`: `[fork].a.bR[-patch-c]`  
If the version is beta, it looks like: `[fork].a.bR-beta-d`

#### What it means 🧩

- **`fork`** — base version of **Mappet**, e.g. `0.9` 🛠️
- **`a`** — main version series; all versions with the same `a` are backward-compatible 🔗
- **`b`** — update within version `a`, such as new features or improvements (may include bugfixes) ✨
- **`patch-c`** — patch (bugfix) for version `a.b`, where `c` is the patch number 🐛
- **`beta-d`** — prerelease for version `a.b`, where `d` is the total number of betas in `a.b` 🧪

#### Examples 📘

- `0.9.5R` — first stable release in series `5` 🎯
- `0.9.5.6R` — update with new features 🚀
- `0.9.5.3R-patch-2` — second patch for version `5.3` (bugfixes only) 🔧
- `0.9.6R-beta-4` — fourth beta version in series `6` (can be built after `0.9.6.x`) 🧪

#### Notes ⚠️

- Different `a` values **may introduce breaking changes** 💥
- Don't download beta versions if you didn't watch **Breaking Bad** ☢️
- Patches are meant **only** for bugfixes 🛡️
- *Maybe adjusted in the future if someone suggests a better approach* 🧑‍💻

---

# Формат версий 📦

Как будут выглядеть версии после `0.9.5.3R`: `[fork].a.bR[-patch-c]`\
Бета версии выглядят так: `[fork].a.bR-beta-d`

#### Что это значит 🧩

- **`fork`** — базовая версия **Mappet**, например `0.9` 🛠️
- **`a`** — основная версия; все версии с одинаковым `a` совместимы друг с другом 🔗
- **`b`** — обновление внутри `a`: новые функции или улучшения (может включать багфиксы) ✨
- **`patch-c`** — патч (исправление багов) к версии `a.b`, где `c` — номер патча 🐛
- **`beta-d`** — предварительная версия `a.b`, где `d` — номер беты в рамках `a.b` 🧪

#### Примеры 📘

- `0.9.5R` — первая стабильная версия в серии `5` 🎯
- `0.9.5.6R` — обновление с новыми возможностями 🚀
- `0.9.5.3R-patch-2` — второй патч к версии `5.3` (только багфиксы) 🔧
- `0.9.6R-beta-4` — четвёртая бета-версия серии `6` (может быть собран после `0.9.6.x`) 🧪

#### Примечания ⚠️

- Между разными значениями `a` возможна **несовместимость** 💥
- Не устанавливайте беты, если не смотрели **Breaking Bad** ☢️
- Патчи предназначены **только** для багфиксов 🛡️
- *Может быть изменено, если кто-нибудь предложит что-то лучше* 🧑‍💻
